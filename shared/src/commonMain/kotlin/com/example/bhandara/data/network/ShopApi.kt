package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.LocalShopRequest
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.data.models.api.ReportShopRequest
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.data.models.api.VerifyShopRequest
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Local shops: details, menu, and what customers and owners can do with a shop */
class ShopApi(private val client: ApiClient) {

    /** Lists a new shop owned by the signed-in user (the backend takes the owner from their sign-in, not the body) */
    suspend fun createShop(shop: LocalShopRequest): ApiResult<LocalShopResponse> =
        client.call("api/localshops") {
            method = HttpMethod.Post
            setBody(shop)
        }

    /** Shops (not moving carts) customers can see within [radiusMeters] of a point */
    suspend fun getNearbyShops(near: GeoLocation, radiusMeters: Double): ApiResult<List<LocalShopResponse>> =
        client.call("api/localshops/nearby") {
            method = HttpMethod.Get
            parameter("latitude", near.latitude)
            parameter("longitude", near.longitude)
            parameter("radius", radiusMeters)
            parameter("isMovingCart", false)
        }

    suspend fun getShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId") { method = HttpMethod.Get }

    /** The menu with prices; empty for older shops that only have a list of item names */
    suspend fun getMenu(shopId: String): ApiResult<List<MenuItemResponse>> =
        client.call("api/menu-items/shop/$shopId") { method = HttpMethod.Get }

    /** Flag wrong or inappropriate information, saying why (and optionally more); a moderator decides. Each user counts once. */
    suspend fun reportShop(
        shopId: String,
        reason: ShopReportReason,
        note: String? = null,
        photoUrls: List<String> = emptyList(),
    ): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId/report") {
            method = HttpMethod.Put
            setBody(ReportShopRequest(reason.name, note?.trim()?.ifEmpty { null }, photoUrls.ifEmpty { null }))
        }

    /** Suggest the shop no longer exists; a moderator decides. Each user counts once. */
    suspend fun suggestDeleteShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId/suggest-delete") { method = HttpMethod.Put }

    /**
     * Answers "Is this place real?" (YES, NO or NOT_SURE) from where the user is now; YES and NO only count from
     * people near the shop. A 409 (conflict) means the user shouldn't be asked again (already voted, own shop, or
     * no longer unverified).
     */
    suspend fun verifyShop(shopId: String, vote: String, from: GeoLocation?): ApiResult<Unit> =
        client.callForNothing("api/localshops/$shopId/verify") {
            method = HttpMethod.Post
            setBody(VerifyShopRequest(vote, from?.latitude, from?.longitude))
        }

    /** The owner takes their own shop off the map */
    suspend fun deactivateShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId/deactivate") { method = HttpMethod.Put }
}
