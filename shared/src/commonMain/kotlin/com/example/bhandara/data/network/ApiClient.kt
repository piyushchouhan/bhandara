package com.example.bhandara.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** The networking stack of the platform the app runs on (OkHttp on Android, NSURLSession on iOS) */
expect fun platformHttpEngine(): HttpClientEngine

/**
 * Sends requests to the Local Feast backend from either platform and turns every outcome into an [ApiResult].
 *
 * @param baseUrl       the backend's address, e.g. "https://api.example.com/"
 * @param idTokenProvider the signed-in user's Firebase ID token, or null when nobody is signed in; it is sent as
 *                      "Authorization: Bearer <token>" on every request
 */
class ApiClient(
    baseUrl: String,
    private val idTokenProvider: suspend () -> String?,
    engine: HttpClientEngine = platformHttpEngine(),
) {
    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true // newer backends may add fields
        explicitNulls = false
        encodeDefaults = true
    }

    @PublishedApi
    internal val http = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 60_000 // reading menu photos with AI can take a while
        }
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }

    /**
     * Sends a request and reads the response body as [T].
     *
     * @param path    relative to the base URL, e.g. "api/localshops/5"
     * @param request sets the method, query parameters and body
     */
    suspend inline fun <reified T> call(
        path: String,
        crossinline request: HttpRequestBuilder.() -> Unit,
    ): ApiResult<T> = send(path, { request() }) { response ->
        json.decodeFromString<T>(response.bodyAsText())
    }

    /** Like [call], for requests whose response body doesn't matter */
    suspend inline fun callForNothing(
        path: String,
        crossinline request: HttpRequestBuilder.() -> Unit,
    ): ApiResult<Unit> = send(path, { request() }) { }

    @PublishedApi
    internal suspend fun <T> send(
        path: String,
        request: HttpRequestBuilder.() -> Unit,
        read: suspend (HttpResponse) -> T,
    ): ApiResult<T> {
        val response = try {
            val token = idTokenProvider()
            http.request(path) {
                if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
                request()
            }
        } catch (e: CancellationException) {
            throw e // the screen went away; don't report it as a failure
        } catch (e: HttpRequestTimeoutException) {
            return ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, ApiErrors.TIMEOUT))
        } catch (e: Exception) {
            // No connection, DNS failure, connection reset, connect timeout, ...
            return ApiResult.Failure(ApiError(ApiError.Kind.NETWORK, ApiErrors.NETWORK))
        }

        if (!response.status.isSuccess()) {
            return ApiResult.Failure(ApiErrors.fromResponse(response.status.value, response.bodyAsText()))
        }
        return try {
            ApiResult.Success(read(response))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SerializationException) {
            ApiResult.Failure(ApiError(ApiError.Kind.UNEXPECTED_RESPONSE, ApiErrors.UNEXPECTED, response.status.value))
        } catch (e: IllegalArgumentException) {
            ApiResult.Failure(ApiError(ApiError.Kind.UNEXPECTED_RESPONSE, ApiErrors.UNEXPECTED, response.status.value))
        }
    }
}
