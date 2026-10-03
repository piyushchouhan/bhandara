package com.example.bhandara.auth

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ShopApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Signing in, the same on Android and iOS, with Firebase replaced by a fake */
class AuthServiceTest {

    /** Firebase stand-in: answers sign-in when [finishSignIn] is called, like the real network round trip */
    private class FakeFirebase(var uid: String? = null) : PlatformAuth {
        var signInRequests = 0
        var tokenRequests = 0
        var tokenError: String? = null
        private val pendingSignIns = mutableListOf<(String?, String?) -> Unit>()

        override fun currentUid() = uid

        override fun signInAnonymously(onResult: (uid: String?, error: String?) -> Unit) {
            signInRequests++
            pendingSignIns += onResult
        }

        fun finishSignIn(newUid: String?, error: String? = null) {
            uid = newUid
            pendingSignIns.toList().forEach { it(newUid, error) }
            pendingSignIns.clear()
        }

        override fun idToken(forceRefresh: Boolean, onResult: (token: String?, error: String?) -> Unit) {
            tokenRequests++
            if (tokenError != null) onResult(null, tokenError) else onResult("token-for-$uid", null)
        }
    }

    @Test
    fun aSignedInUserIsNotSignedInAgain() = runTest {
        val firebase = FakeFirebase(uid = "existing-user")

        val result = AuthService(firebase).ensureSignedIn()

        assertEquals("existing-user", result.getOrThrow())
        assertEquals(0, firebase.signInRequests)
    }

    @Test
    fun aNewUserIsSignedInAnonymously() = runTest {
        val firebase = FakeFirebase()
        val auth = AuthService(firebase)

        val result = async { auth.ensureSignedIn() }
        yield()
        firebase.finishSignIn("new-user")

        assertEquals("new-user", result.await().getOrThrow())
        assertEquals("new-user", auth.currentUid)
    }

    @Test
    fun signInsStartedAtTheSameTimeCreateOnlyOneAccount() = runTest {
        val firebase = FakeFirebase()
        val auth = AuthService(firebase)

        // e.g. app start and the first request
        val first = async { auth.ensureSignedIn() }
        val second = async { auth.ensureSignedIn() }
        yield()
        firebase.finishSignIn("new-user")

        assertEquals("new-user", first.await().getOrThrow())
        assertEquals("new-user", second.await().getOrThrow())
        assertEquals(1, firebase.signInRequests)
    }

    @Test
    fun aFailedSignInIsReportedAndTriedAgainNextTime() = runTest {
        val firebase = FakeFirebase()
        val auth = AuthService(firebase)

        val failed = async { auth.ensureSignedIn() }
        yield()
        firebase.finishSignIn(null, error = "Network error")
        val error = failed.await().exceptionOrNull()
        assertIs<SignInException>(error)
        assertEquals("Network error", error.message)

        val retried = async { auth.ensureSignedIn() }
        yield()
        firebase.finishSignIn("new-user")
        assertEquals("new-user", retried.await().getOrThrow())
        assertEquals(2, firebase.signInRequests)
    }

    @Test
    fun theIdTokenIsOnlyAskedForWhenSomeoneIsSignedIn() = runTest {
        val signedOut = FakeFirebase()
        assertNull(AuthService(signedOut).idToken())
        assertEquals(0, signedOut.tokenRequests)

        assertEquals("token-for-me", AuthService(FakeFirebase(uid = "me")).idToken())
    }

    @Test
    fun aTokenThatCannotBeFetchedMeansNoToken() = runTest {
        val firebase = FakeFirebase(uid = "me").apply { tokenError = "Token expired and refresh failed" }

        assertNull(AuthService(firebase).idToken())
    }

    @Test
    fun backendRequestsCarryTheSignedInUsersToken() = runTest {
        val auth = AuthService(FakeFirebase(uid = "me"))
        var sentAuthorization: String? = null
        val engine = MockEngine { request ->
            sentAuthorization = request.headers[HttpHeaders.Authorization]
            respond("""{"id": "5", "shopName": "Sharma Chaat", "latitude": 18.58, "longitude": 73.88}""",
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        ShopApi(ApiClient("https://api.test/", auth::idToken, engine)).getShop("5")

        assertEquals("Bearer token-for-me", sentAuthorization)
    }
}
