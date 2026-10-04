package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.MenuItemResponse
import com.example.bhandara.data.models.api.VerifyShopRequest
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Local shops: details, menu, and what customers and owners can do with a shop */
class ShopApi(private val client: ApiClient) {

    suspend fun getShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId") { method = HttpMethod.Get }

    /** The menu with prices; empty for older shops that only have a list of item names */
    suspend fun getMenu(shopId: String): ApiResult<List<MenuItemResponse>> =
        client.call("api/menu-items/shop/$shopId") { method = HttpMethod.Get }

    /** Flag wrong or inappropriate information; a moderator decides. Each user counts once. */
    suspend fun reportShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId/report") { method = HttpMethod.Put }

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
