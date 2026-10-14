package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.ManualMenuItemsRequest
import com.example.bhandara.data.models.api.MenuItemExtractResponse
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.photos.CompressedPhoto
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

/** A shop's menu: its owner adding items, typed in or read from photos of the menu by AI */
class MenuApi(private val client: ApiClient) {

    /** Adds items to the owner's shop menu; a name already on the menu (or twice in [items]) is a 409 */
    suspend fun addItems(shopId: String, items: List<MenuItemRequest>): ApiResult<List<MenuItemResponse>> =
        client.call("api/menu-items/manual/shop/$shopId") {
            method = HttpMethod.Post
            setBody(ManualMenuItemsRequest(items))
        }

    /**
     * Reads the items off photos of a menu with AI, for the user to check before saving. Paid per photo, so it's
     * limited to a few scans in a row (429 after that).
     */
    suspend fun readMenuPhotos(photos: List<CompressedPhoto>): ApiResult<List<MenuItemRequest>> =
        client.call<MenuItemExtractResponse>("api/menu-items/image/extract") {
            method = HttpMethod.Post
            // The form sets its own content type (multipart, with a boundary) instead of the client's JSON default
            headers.remove(HttpHeaders.ContentType)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        photos.forEachIndexed { index, photo ->
                            append(
                                "images",
                                photo.bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, photo.contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"menu_${index + 1}.${photo.extension}\"")
                                },
                            )
                        }
                    }
                )
            )
        }.map { it.items }
}
