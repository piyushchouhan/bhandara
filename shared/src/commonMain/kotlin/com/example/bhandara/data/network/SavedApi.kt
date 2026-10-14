package com.example.bhandara.data.network

import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

/** A saved shop or feast as the account keeps it: [kind] "SHOP" or "FEAST", [savedAt] ms since 1970 */
@Serializable
data class SavedPlaceDto(val kind: String, val id: String, val savedAt: Long? = null)

/** The shops and feasts the signed-in user saved, kept with their account */
class SavedApi(private val client: ApiClient) {

    @Serializable
    private data class SyncRequest(val saved: List<SavedPlaceDto>, val removed: List<SavedPlaceDto>)

    /** Sends what this phone saved and unsaved since it last synced; returns everything the account has saved */
    suspend fun sync(saved: List<SavedPlaceDto>, removed: List<SavedPlaceDto>): ApiResult<List<SavedPlaceDto>> =
        client.call("api/saved/sync") {
            method = HttpMethod.Post
            setBody(SyncRequest(saved, removed))
        }
}
