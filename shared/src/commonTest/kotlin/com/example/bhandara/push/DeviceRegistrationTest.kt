package com.example.bhandara.push

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A phone registers with the backend once per account, the same on Android and iOS, with or without push
 * notifications, and then shares its location and keeps its push token current. The backend is a fake.
 */
class DeviceRegistrationTest {

    private val dispatcher = StandardTestDispatcher()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var registerStatus = HttpStatusCode.Created
    /** The backend lost the user (e.g. its data was reset): updates answer 404 until it registers again */
    private var userGone = false

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@DeviceRegistrationTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when {
                request.method == HttpMethod.Post && request.url.encodedPath == "/api/users" -> {
                    if (registerStatus == HttpStatusCode.Created) userGone = false
                    registerStatus to if (registerStatus == HttpStatusCode.Created) """{"id":"1","firebaseUid":"me-1"}""" else """{"message":"No"}"""
                }
                userGone -> HttpStatusCode.NotFound to """{"message":"User not found"}"""
                else -> HttpStatusCode.NoContent to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    private var uid: String? = "me-1"
    private var token: String? = null
    private val registration = DeviceRegistration(UserApi(client), settings, { uid }, { token })

    private val here = GeoLocation(18.5823, 73.8845)

    private fun HttpRequestData.json(): JsonObject = Json.parseToJsonElement((body as TextContent).text).jsonObject
    private fun sentTo(method: HttpMethod, path: String) = requests.filter { it.method == method && it.url.encodedPath == path }

    // ==================== Tests ====================

    @Test
    fun aPhoneWithoutPushNotificationsRegistersOnce() = runTest(dispatcher) {
        assertTrue(registration.ensureRegistered())
        assertTrue(registration.ensureRegistered())

        val register = sentTo(HttpMethod.Post, "/api/users").single()
        assertNull(register.json()["fcmToken"], "no token is sent rather than an empty one")
        assertEquals("me-1", register.json()["firebaseUid"]!!.jsonPrimitive.content)
        assertTrue(settings.isRegisteredWithBackend("me-1"))
    }

    @Test
    fun aPhoneWithAPushTokenRegistersWithItAndANewTokenFollowsLater() = runTest(dispatcher) {
        token = "token-1"
        registration.ensureRegistered()
        assertEquals("token-1", sentTo(HttpMethod.Post, "/api/users").single().json()["fcmToken"]!!.jsonPrimitive.content)

        token = "token-2"
        registration.ensureRegistered()
        registration.ensureRegistered()

        val sent = sentTo(HttpMethod.Put, "/api/users/push-token")
        assertEquals(1, sent.size, "the same token isn't sent twice")
        assertEquals("token-2", sent.single().json()["fcmToken"]!!.jsonPrimitive.content)
    }

    @Test
    fun aTokenThatArrivesAfterRegisteringIsSent() = runTest(dispatcher) {
        registration.ensureRegistered()

        token = "late-token"
        registration.ensureRegistered()

        assertEquals("late-token", sentTo(HttpMethod.Put, "/api/users/push-token").single().json()["fcmToken"]!!.jsonPrimitive.content)
    }

    @Test
    fun whenRegisteringFailsItIsTriedAgainNextTime() = runTest(dispatcher) {
        registerStatus = HttpStatusCode.InternalServerError
        assertFalse(registration.ensureRegistered())
        assertFalse(settings.isRegisteredWithBackend("me-1"))

        registerStatus = HttpStatusCode.Created
        assertTrue(registration.ensureRegistered())
        assertEquals(2, sentTo(HttpMethod.Post, "/api/users").size)
    }

    @Test
    fun sharingTheLocationRegistersFirstIfNeeded() = runTest(dispatcher) {
        assertTrue(registration.shareLocation(here))

        val register = sentTo(HttpMethod.Post, "/api/users").single().json()
        assertEquals("18.5823", register["latitude"]!!.jsonPrimitive.content)
        val update = sentTo(HttpMethod.Put, "/api/users/location").single().json()
        assertEquals("73.8845", update["longitude"]!!.jsonPrimitive.content)
        assertNull(update["fcmToken"])
    }

    @Test
    fun withNobodySignedInNothingIsSent() = runTest(dispatcher) {
        uid = null

        assertFalse(registration.ensureRegistered())
        assertFalse(registration.shareLocation(here))
        assertTrue(requests.isEmpty())
    }

    @Test
    fun aPushTokenThatCannotBeHadDoesNotStopRegistering() = runTest(dispatcher) {
        val failing = DeviceRegistration(UserApi(client), settings, { uid }, { error("no Play services") })

        assertTrue(failing.ensureRegistered())
        assertNull(sentTo(HttpMethod.Post, "/api/users").single().json()["fcmToken"])
    }

    @Test
    fun whenTheBackendNoLongerKnowsThePhoneSharingTheLocationRegistersItAgain() = runTest(dispatcher) {
        registration.ensureRegistered()
        userGone = true

        assertTrue(registration.shareLocation(here))

        assertEquals(2, sentTo(HttpMethod.Post, "/api/users").size)
        assertEquals("18.5823", sentTo(HttpMethod.Post, "/api/users").last().json()["latitude"]!!.jsonPrimitive.content)
        assertTrue(settings.isRegisteredWithBackend("me-1"))
    }

    @Test
    fun whenTheBackendNoLongerKnowsThePhoneANewTokenRegistersItAgain() = runTest(dispatcher) {
        token = "token-1"
        registration.ensureRegistered()
        userGone = true

        token = "token-2"
        assertTrue(registration.ensureRegistered())

        assertEquals("token-2", sentTo(HttpMethod.Post, "/api/users").last().json()["fcmToken"]!!.jsonPrimitive.content)
        assertEquals("token-2", settings.lastPushTokenSent("me-1"))
    }

    @Test
    fun otherFailuresDoNotStartARegistrationAgain() = runTest(dispatcher) {
        registration.ensureRegistered()
        registerStatus = HttpStatusCode.InternalServerError

        registration.shareLocation(here)

        assertEquals(1, sentTo(HttpMethod.Post, "/api/users").size, "a location update that worked needs nothing more")
    }

}
