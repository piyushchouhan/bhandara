package com.example.bhandara.push

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Keeping the backend's copy of the phone's push token current, against a fake backend */
class PushTokenSyncTest {

    private val requests = mutableListOf<HttpRequestData>()
    private var backendStatus = HttpStatusCode.NoContent

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    private val sync = PushTokenSync(
        UserApi(ApiClient("https://api.test/", { "id-token" }, MockEngine { request ->
            requests += request
            respond("", backendStatus)
        })),
        settings,
    )

    private fun sentTokens() = requests.map { (it.body as OutgoingContent.ByteArrayContent).bytes().decodeToString() }

    @Test
    fun aNewTokenIsSentToTheBackend() = runTest {
        assertTrue(sync.sync("user-a", "token-1"))

        val request = requests.single()
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("https://api.test/api/users/push-token", request.url.toString())
        assertEquals(listOf("""{"fcmToken":"token-1"}"""), sentTokens())
    }

    @Test
    fun theSameTokenIsNotSentAgainOnEveryAppStart() = runTest {
        sync.sync("user-a", "token-1")
        sync.sync("user-a", "token-1")
        sync.sync("user-a", "token-1")

        assertEquals(1, requests.size)
    }

    @Test
    fun aReplacedTokenIsSent() = runTest {
        sync.sync("user-a", "token-1")
        sync.sync("user-a", "token-2")

        assertEquals(listOf("""{"fcmToken":"token-1"}""", """{"fcmToken":"token-2"}"""), sentTokens())
    }

    @Test
    fun aTokenThatCouldNotBeSentIsTriedAgainNextTime() = runTest {
        backendStatus = HttpStatusCode.ServiceUnavailable
        assertFalse(sync.sync("user-a", "token-1"))

        backendStatus = HttpStatusCode.NoContent
        assertTrue(sync.sync("user-a", "token-1"))
        assertEquals(2, requests.size)
    }

    @Test
    fun aUserNotRegisteredYetIsTriedAgainNextTime() = runTest {
        backendStatus = HttpStatusCode.NotFound
        assertFalse(sync.sync("user-a", "token-1"))

        backendStatus = HttpStatusCode.NoContent
        assertTrue(sync.sync("user-a", "token-1"))
    }

    @Test
    fun eachUserOnThePhoneHasTheirOwnToken() = runTest {
        sync.sync("user-a", "token-1")
        sync.sync("user-b", "token-1") // e.g. after signing in with Google

        assertEquals(2, requests.size)
    }

    @Test
    fun aBlankTokenIsNeverSent() = runTest {
        assertFalse(sync.sync("user-a", " "))
        assertTrue(requests.isEmpty())
    }
}
