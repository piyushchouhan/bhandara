package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * What the Android app does in the background (registering the user, keeping their location for feast
 * notifications, and crowd pings) through the shared client, as the backend expects it
 */
class BackgroundApisTest {

    private val requests = mutableListOf<HttpRequestData>()
    private var body = "{}"

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine { request ->
        requests += request
        respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
    })

    private fun HttpRequestData.json() =
        Json.parseToJsonElement((this.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject

    @Test
    fun aUserIsRegisteredWithTheirPushToken() = runTest {
        body = """{"id":1,"firebaseUid":"u1","fcmToken":"push","latitude":18.58,"longitude":73.88}"""

        UserApi(client).register("u1", "push", GeoLocation(18.58, 73.88))

        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/users", request.url.encodedPath)
        assertEquals("push", request.json()["fcmToken"]!!.jsonPrimitive.content)
        assertEquals(18.58, request.json()["latitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun withoutALocationTheUserIsRegisteredNowhereNear() = runTest {
        body = """{"id":1,"firebaseUid":"u1","fcmToken":"push","latitude":-90.0,"longitude":-180.0}"""

        UserApi(client).register("u1", "push", null)

        assertEquals(-90.0, requests.single().json()["latitude"]!!.jsonPrimitive.double)
        assertEquals(-180.0, requests.single().json()["longitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun theUsersLocationIsKeptUpToDate() = runTest {
        val result = UserApi(client).updateLocation("u1", "push", GeoLocation(18.6, 73.9))

        assertIs<ApiResult.Success<Unit>>(result)
        val request = requests.single()
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("/api/users/location", request.url.encodedPath)
        assertEquals(73.9, request.json()["longitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun aCrowdPingSaysWhereThePhoneIs() = runTest {
        CrowdApi(client).ping(GeoLocation(18.58, 73.88))

        val request = requests.single()
        assertEquals("/api/crowd/ping", request.url.encodedPath)
        assertEquals(18.58, request.json()["latitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun nearbyShopsLeaveOutMovingCarts() = runTest {
        body = """[{"id":"5","shopName":"Chai Point","latitude":18.58,"longitude":73.88}]"""

        val shops = ShopApi(client).getNearbyShops(GeoLocation(18.58, 73.88), 300.0)

        val url = requests.single().url
        assertEquals("/api/localshops/nearby", url.encodedPath)
        assertEquals("false", url.parameters["isMovingCart"])
        assertEquals("300.0", url.parameters["radius"])
        assertEquals(listOf("Chai Point"), (shops as ApiResult.Success).value.map { it.shopName })
    }
}
