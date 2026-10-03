package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
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

    /** The owner takes their own shop off the map */
    suspend fun deactivateShop(shopId: String): ApiResult<LocalShopResponse> =
        client.call("api/localshops/$shopId/deactivate") { method = HttpMethod.Put }
}
