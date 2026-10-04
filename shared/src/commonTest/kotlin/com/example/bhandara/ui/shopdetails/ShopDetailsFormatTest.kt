package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.models.api.LocalShopResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun onlyFacilitiesTheShopSaysItHasAreListed() {
        val shop = shop().copy(hasSeating = true, homeDelivery = false, takeaway = true, wifiAvailable = null, onlinePayment = true)
        assertEquals(listOf(Facility.SEATING, Facility.TAKEAWAY, Facility.ONLINE_PAYMENT), facilities(shop))
        assertEquals(emptyList(), facilities(shop()))
    }
}
