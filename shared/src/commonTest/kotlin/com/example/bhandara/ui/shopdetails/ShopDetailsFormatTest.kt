package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.models.api.MenuItemResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** How the shop details page writes a shop's times, address, prices and facilities */
class ShopDetailsFormatTest {

    private fun shop() = LocalShopResponse(id = "1", shopName = "Sharma Chaat", latitude = 18.58, longitude = 73.88)

    @Test
    fun timesAreShownOnATwelveHourClock() {
        assertEquals("9:00 AM", formatShopTime("09:00:00"))
        assertEquals("1:30 PM", formatShopTime("13:30:00"))
        assertEquals("12:00 PM", formatShopTime("12:00"))
        assertEquals("12:15 AM", formatShopTime("00:15:00"))
        assertEquals("11:59 PM", formatShopTime("23:59:59"))
    }

    @Test
    fun aTimeThatCannotBeReadIsShownAsItCame() {
        assertEquals("evening", formatShopTime("evening"))
        assertEquals("25:00", formatShopTime("25:00"))
    }

    @Test
    fun openingHoursNeedBothTimes() {
        assertEquals("9:00 AM – 10:00 PM", openingHours(shop().copy(openingTime = "09:00:00", closingTime = "22:00:00")))
        assertNull(openingHours(shop().copy(openingTime = "09:00:00")))
        assertNull(openingHours(shop().copy(openingTime = " ", closingTime = "22:00:00")))
    }

    @Test
    fun theFullAddressIsPreferredThenAreaAndCity() {
        assertEquals("12 MG Road, Pune", shopAddress(shop().copy(fullAddress = "12 MG Road, Pune", area = "Camp")))
        assertEquals("Vishrantwadi, Pune", shopAddress(shop().copy(fullAddress = "  ", area = "Vishrantwadi", city = "Pune")))
        assertEquals("Pune", shopAddress(shop().copy(area = "", city = "Pune")))
        assertNull(shopAddress(shop()))
    }

    @Test
    fun aLandmarkIsNotShownAsNearNear() {
        assertEquals("Datta Mandir", landmarkName("Datta Mandir"))
        assertEquals("Dhanori Junction", landmarkName("Near Dhanori Junction"))
        assertEquals("Dhanori Junction", landmarkName("  near: Dhanori Junction "))
        assertEquals("Nearby Bakery", landmarkName("Nearby Bakery"))
        assertNull(landmarkName("Near"))
        assertNull(landmarkName("  "))
        assertNull(landmarkName(null))
    }

    @Test
    fun theTypeLineJoinsWhatIsKnown() {
        assertEquals("Restaurant • South Indian", shopTypeLine(shop().copy(shopType = "Restaurant", cuisineType = "South Indian")))
        assertEquals("Cafe", shopTypeLine(shop().copy(shopType = "Cafe", cuisineType = " ")))
        assertNull(shopTypeLine(shop()))
    }

    @Test
    fun pricesShowPaiseOnlyWhenThereAreAny() {
        assertEquals("₹20", formatPrice(20.0))
        assertEquals("₹20.50", formatPrice(20.5))
        assertEquals("₹0.05", formatPrice(0.05))
        assertEquals("₹100", formatPrice(99.999))
    }

    @Test
    fun aRateSaysWhatItIsFor() {
        assertEquals("₹40 / kg", formatPrice(40.0, "kg"))
        assertEquals("₹200 / tray of 30", formatPrice(200.0, " tray of 30 "))
        assertEquals("₹40", formatPrice(40.0, ""))
        assertEquals("₹40", formatPrice(40.0, null))
    }

    @Test
    fun onlyFacilitiesTheShopSaysItHasAreListed() {
        val shop = shop().copy(hasSeating = true, homeDelivery = false, takeaway = true, wifiAvailable = null, onlinePayment = true)
        assertEquals(listOf(Facility.SEATING, Facility.TAKEAWAY, Facility.ONLINE_PAYMENT), facilities(shop))
        assertEquals(emptyList(), facilities(shop()))
    }

    @Test
    fun ratingsShowOneDecimal() {
        assertEquals("3.0", formatRating(3.0))
        assertEquals("4.3", formatRating(4.25))
        assertEquals("5.0", formatRating(4.96))
    }

    @Test
    fun theKindLineShowsTheCuisineElseTheShopType() {
        assertEquals("Street food", shopKind(shop().copy(shopType = "Restaurant", cuisineType = "Street food")))
        assertEquals("Cafe", shopKind(shop().copy(shopType = "Cafe", cuisineType = " ")))
        assertNull(shopKind(shop()))
    }

    @Test
    fun theBusierTheShopTheMoreBarsAreLit() {
        assertEquals(1, crowdBars("QUIET"))
        assertEquals(2, crowdBars("MODERATE"))
        assertEquals(4, crowdBars("BUSY"))
        assertEquals(0, crowdBars(null))
    }

    @Test
    fun theVegFilterShowsOnlyForMixedMenus() {
        fun item(type: String) = MenuItemResponse(id = type, shopId = "1", name = type, foodType = type, isAvailable = true)
        assertTrue(showsVegFilter(listOf(item("VEG"), item("NON_VEG"))))
        assertTrue(showsVegFilter(listOf(item("JAIN"), item("EGG"))))
        assertFalse(showsVegFilter(listOf(item("VEG"), item("VEGAN"))), "an all-veg menu needs no filter")
        assertFalse(showsVegFilter(listOf(item("NON_VEG"))))
        assertFalse(showsVegFilter(emptyList()))
    }

    @Test
    fun eggDishesAreLeftOutOfMenusOnlyWhenTheUserTurnsThemOff() {
        fun item(type: String) = MenuItemResponse(id = type, shopId = "1", name = type, foodType = type, isAvailable = true)
        val menu = listOf(item("VEG"), item("EGG"), item("NON_VEG"))

        assertEquals(listOf("VEG", "EGG", "NON_VEG"), menuWithPreferences(menu, showEggItems = true).map { it.foodType })
        assertEquals(listOf("VEG", "NON_VEG"), menuWithPreferences(menu, showEggItems = false).map { it.foodType })
    }

    @Test
    fun aCartsStopTimesReadLikeAClock() {
        assertEquals("7:00 – 9:30 AM", stopTimes("07:00:00", "09:30:00", timesVary = false))
        assertEquals("11:30 AM – 1:00 PM", stopTimes("11:30:00", "13:00:00", timesVary = false))
        assertEquals("4:00 PM", stopTimes("16:00:00", null, timesVary = false))
        assertNull(stopTimes("07:00:00", "09:00:00", timesVary = true))
        assertNull(stopTimes(null, null, timesVary = false))
        assertNull(stopTimes("soon", null, timesVary = false))
    }

    @Test
    fun customersSeeOnlyTheCartSettingsThatMatterToThemEachOnce() {
        val options = mapOf(
            "Milk & dairy/home_delivery" to true,
            "Milk & dairy/monthly_bill" to false,
            "Sabzi/daily_rates" to true, // the vendor's own reminder
            "Eggs/delivers_homes" to true,
            "Bread & pav/delivers_homes" to true,
        )

        assertEquals(listOf("home_delivery", "delivers_homes"), cartOptionsOn(options))
    }

    @Test
    fun aCartsDetailsAreReadFromTheShop() {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val shop = json.decodeFromString(
            LocalShopResponse.serializer(),
            """{"id":"7","shopName":"Ramesh Bhel","latitude":18.5,"longitude":73.8,"isMovingCart":true,
                "cartDetails":{"cartType":"CYCLE","sells":["Chaat & snacks"],"stops":[{"name":"MIT gate","from":"07:00:00","to":null,"timesVary":false}],
                "daysOut":["MON"],"payments":["UPI"],"spotHint":"Blue umbrella","options":{"Chaat & snacks/pure_veg":true},"newField":1}}""",
        )

        val cart = shop.cartDetails!!
        assertEquals("CYCLE", cart.cartType)
        assertEquals("MIT gate", cart.stops.single().name)
        assertEquals(listOf("UPI"), cart.payments)
        assertFalse(cart.noFixedRoute)
        assertNull(json.decodeFromString(LocalShopResponse.serializer(), """{"id":"8","shopName":"Sweets","latitude":1.0,"longitude":2.0}""").cartDetails)
    }

    @Test
    fun countsAreShortenedToFitASmallStat() {
        assertEquals("0", compactCount(0))
        assertEquals("640", compactCount(640))
        assertEquals("999", compactCount(999))
        assertEquals("1k", compactCount(1_000))
        assertEquals("1.2k", compactCount(1_249))
        assertEquals("9.9k", compactCount(9_999))
        assertEquals("12k", compactCount(12_400))
        assertEquals("1.5M", compactCount(1_520_000))
    }

}
