package com.example.bhandara.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Tapping a notification opens what it's about, the same on every platform */
class NotificationRoutesTest {

    @Test
    fun eachKindOfNotificationOpensItsPage() {
        assertEquals(Route.FeastDetails("9"), routeForNotification(mapOf("type" to "new_localfeast", "feastId" to "9")))
        assertEquals(Route.MyReports, routeForNotification(mapOf("type" to "report_update", "kind" to "SHOP", "shopId" to "5")))
        assertEquals(Route.ShopDetails("5"), routeForNotification(mapOf("type" to "shop_review", "shopId" to "5")))
        assertEquals(Route.ManageShop("5"), routeForNotification(mapOf("type" to "claim_status", "shopId" to "5", "approved" to "true")))
        assertEquals(Route.ShopDetails("5"), routeForNotification(mapOf("type" to "claim_status", "shopId" to "5", "approved" to "false")))
    }

    @Test
    fun anythingElseJustOpensTheApp() {
        assertNull(routeForNotification(emptyMap()))
        assertNull(routeForNotification(mapOf("type" to "something_new", "shopId" to "5")))
        assertNull(routeForNotification(mapOf("type" to "new_localfeast", "feastId" to " ")), "a feast without its id")
        assertNull(routeForNotification(mapOf("type" to "shop_review", "shopId" to null)))
    }
}
