package com.example.bhandara.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Turns a failed response into an [ApiError] with a message for the user.
 *
 * The backend writes user-facing explanations for invalid requests, refusals and conflicts (e.g. "You have already
 * reviewed this shop", "Rating must be between 1 and 5"), so those are shown as they are. For other failures its
 * text may be technical ("Local shop not found with ID: 5") or internal, so a friendly message is used instead.
 */
internal object ApiErrors {

    const val NETWORK = "No internet connection. Check your connection and try again."
    const val TIMEOUT = "The server is taking too long to respond. Please try again."
    const val UNAUTHORIZED = "Please sign in again."
    const val FORBIDDEN = "You're not allowed to do that."
    const val NOT_FOUND = "This is no longer available. It may have been removed."
    const val RATE_LIMITED = "You're doing that too often. Please wait a while and try again."
    const val UNAVAILABLE = "The service is temporarily unavailable. Please try again in a moment."
    const val SERVER = "Something went wrong on our side. Please try again."
    const val UNEXPECTED = "Something went wrong. Please try again."

    private val json = Json { ignoreUnknownKeys = true }

    fun fromResponse(status: Int, body: String): ApiError {
        val parsed = parse(body)
        val serverMessage = parsed?.get("message")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        val fieldErrors = parsed?.get("errors")?.let { errors ->
            runCatching {
                errors.jsonObject.mapValues { (_, value) -> value.jsonPrimitive.content }
            }.getOrNull()
        }.orEmpty()

        return when (status) {
            400 -> ApiError(
                ApiError.Kind.INVALID,
                // "Validation failed" says nothing useful; the first field's problem does
                fieldErrors.values.firstOrNull() ?: serverMessage ?: UNEXPECTED,
                status,
                fieldErrors,
            )
            401 -> ApiError(ApiError.Kind.UNAUTHORIZED, UNAUTHORIZED, status)
            403 -> ApiError(ApiError.Kind.FORBIDDEN, serverMessage ?: FORBIDDEN, status)
            404 -> ApiError(ApiError.Kind.NOT_FOUND, NOT_FOUND, status)
            409 -> ApiError(ApiError.Kind.CONFLICT, serverMessage ?: UNEXPECTED, status)
            429 -> ApiError(ApiError.Kind.RATE_LIMITED, RATE_LIMITED, status)
            // The backend's 503s explain themselves (e.g. "The AI service is unavailable right now...")
            503 -> ApiError(ApiError.Kind.UNAVAILABLE, serverMessage ?: UNAVAILABLE, status)
            in 500..599 -> ApiError(ApiError.Kind.SERVER, SERVER, status)
            else -> ApiError(ApiError.Kind.UNEXPECTED_RESPONSE, serverMessage ?: UNEXPECTED, status)
        }
    }

    private fun parse(body: String): JsonObject? =
        runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
}
