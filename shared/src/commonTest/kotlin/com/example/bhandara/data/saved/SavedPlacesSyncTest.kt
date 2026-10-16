package com.example.bhandara.data.saved

import com.example.bhandara.auth.UserAccount
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.SavedApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Saved places on the phone and with the account stay the same, on Android and iOS: changes wait in a queue (also
 * offline), each sync sends them and brings back everything the account has, and one account's places never end up
 * in another's. The backend is a fake.
 */
class SavedPlacesSyncTest {

    private val dispatcher = StandardTestDispatcher()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    /** What the account has saved, as the backend answers; null answers with an error */
    private var accountHas: String? = "[]"
    /** Runs while the request is on its way, e.g. the user saving something meanwhile */
    private var meanwhile: () -> Unit = {}

    private var clock = 1_000L
    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    }, nowMillis = { clock })

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@SavedPlacesSyncTest.dispatcher
        addHandler { request ->
            requests += request
            meanwhile()
            val body = accountHas
            if (body == null) respond("""{"message":"Server error"}""", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, "application/json"))
            else respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private var signedIn: UserAccount? = UserAccount(uid = "guest-1", isAnonymous = true)
    private val sync = SavedPlacesSync(settings, SavedApi(client)) { signedIn }

    private fun sent(): JsonObject = Json.parseToJsonElement((requests.last().body as TextContent).text).jsonObject

    /** "KIND:id" of each place in the request's list */
    private fun JsonObject.keys(list: String) = this[list]!!.jsonArray.map { it.jsonObject["kind"]!!.jsonPrimitive.content + ":" + it.jsonObject["id"]!!.jsonPrimitive.content }.toSet()

    private fun places(vararg keys: String) = keys.joinToString(",", "[", "]") { key ->
        val (kind, id) = key.split(':')
        """{"kind":"$kind","id":"$id","savedAt":500}"""
    }

    // ==================== Tests ====================

    @Test
    fun theFirstSyncSendsEverythingSavedOnThePhoneAndBringsBackWhatTheAccountHas() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        settings.setFeastSaved("9", true)
        accountHas = places("SHOP:5", "FEAST:9", "SHOP:7") // 7 was saved on another phone

        assertTrue(sync.sync())

        assertEquals(setOf("SHOP:5", "FEAST:9"), sent().keys("saved"))
        assertEquals(setOf("5", "7"), settings.savedShopIds.toSet())
        assertEquals(listOf("9"), settings.savedFeastIds)
        assertEquals(500L, settings.savedAt(AppSettings.KEY_SAVED_SHOP, "7"))
        assertTrue(settings.pendingSavedChanges().isEmpty())
    }

    @Test
    fun anUnsaveIsSentOnceAndThenTheQueueIsEmpty() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        accountHas = places("SHOP:5")
        sync.sync()

        settings.setShopSaved("5", false)
        accountHas = "[]"
        sync.sync()

        assertEquals(setOf("SHOP:5"), sent().keys("removed"))
        assertTrue(sent().keys("saved").isEmpty())
        assertTrue(settings.savedShopIds.isEmpty())

        sync.sync()
        assertTrue(sent().keys("saved").isEmpty() && sent().keys("removed").isEmpty(), "nothing is sent twice")
    }

    @Test
    fun offlineTheChangesWaitAndThePhoneKeepsShowingThem() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        accountHas = null

        assertFalse(sync.sync())

        assertEquals(listOf("5"), settings.savedShopIds)
        assertEquals(1, settings.pendingSavedChanges().size)

        accountHas = places("SHOP:5")
        assertTrue(sync.sync())
        assertEquals(setOf("SHOP:5"), sent().keys("saved"))
    }

    @Test
    fun whatIsSavedWhileASyncIsOnItsWayIsKeptForTheNextOne() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        accountHas = places("SHOP:5")
        meanwhile = { settings.setShopSaved("8", true) }

        sync.sync()

        assertEquals(setOf("5", "8"), settings.savedShopIds.toSet())
        assertEquals(listOf("8"), settings.pendingSavedChanges().map { it.id })
    }

    @Test
    fun aGuestsPlacesGoIntoTheAccountTheySignInTo() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        accountHas = places("SHOP:5")
        sync.sync()

        // Signs in to a Google account they already had (a new uid)
        signedIn = UserAccount(uid = "google-1", isAnonymous = false)
        accountHas = places("SHOP:5", "SHOP:3")
        sync.sync()

        assertEquals(setOf("SHOP:5"), sent().keys("saved"))
        assertEquals(setOf("5", "3"), settings.savedShopIds.toSet())
    }

    @Test
    fun afterSigningOutTheAccountsPlacesLeaveThePhoneAndDoNotGoToTheNextAccount() = runTest(dispatcher) {
        signedIn = UserAccount(uid = "google-1", isAnonymous = false)
        settings.setShopSaved("5", true)
        accountHas = places("SHOP:5")
        sync.sync()

        // Signs out: the app signs in again as a new guest
        signedIn = UserAccount(uid = "guest-2", isAnonymous = true)
        accountHas = "[]"
        sync.sync()

        assertTrue(sent().keys("saved").isEmpty())
        assertTrue(settings.savedShopIds.isEmpty())
    }

    @Test
    fun withNobodySignedInNothingIsSent() = runTest(dispatcher) {
        signedIn = null
        settings.setShopSaved("5", true)

        assertFalse(sync.sync())

        assertTrue(requests.isEmpty())
        assertEquals(listOf("5"), settings.savedShopIds)
    }

    @Test
    fun eachSaveOrUnsaveIsAChangeToSync() {
        val before = settings.savedChanges.value

        settings.setShopSaved("5", true)
        settings.setFeastSaved("9", false)

        assertEquals(before + 2, settings.savedChanges.value)
    }

    @Test
    fun aSyncIsNotItselfAChangeToSyncSoSyncingStops() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        accountHas = places("SHOP:5", "SHOP:7")
        val before = settings.savedChanges.value

        sync.sync()

        assertEquals(before, settings.savedChanges.value, "only the user's saves and unsaves start a sync")
    }

}
