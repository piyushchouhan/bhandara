package com.example.bhandara.data.network

/**
 * The outcome of a call to the backend: the value, or an [ApiError] with a message that can be shown to the user
 * as is. Screens never have to guess why something failed.
 */
sealed interface ApiResult<out T> {

    data class Success<T>(val value: T) : ApiResult<T>

    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

/**
 * Why a call failed.
 *
 * @param message      safe to show the user: the backend's own explanation when it gives one meant for users,
 *                     otherwise a friendly message for the [kind] of failure (never a stack trace or raw response)
 * @param fieldErrors  for invalid input, the problem with each field (e.g. "rating" -> "Rating must be between 1 and 5")
 */
data class ApiError(
    val kind: Kind,
    val message: String,
    val status: Int? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
) {
    enum class Kind {
        /** No connection, or the server took too long */
        NETWORK,

        /** Not signed in, or the sign-in expired (401) */
        UNAUTHORIZED,

        /** Signed in, but not allowed (403) */
        FORBIDDEN,

        /** It doesn't exist (any more) (404) */
        NOT_FOUND,

        /** Conflicts with what's already there, e.g. a duplicate (409) */
        CONFLICT,

        /** The request was invalid (400) */
        INVALID,

        /** Too many requests in a short time (429) */
        RATE_LIMITED,

        /** The server or a service it depends on is temporarily down (503) */
        UNAVAILABLE,

        /** Something broke on the server (other 5xx) */
        SERVER,

        /** The server answered with something the app couldn't read */
        UNEXPECTED_RESPONSE,
    }
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(value)
    return this
}

inline fun <T> ApiResult<T>.onFailure(action: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(error)
    return this
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.value
