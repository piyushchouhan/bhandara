package com.example.bhandara.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Links handed to other apps: the phone dialer and Google Maps */
class ExternalLinksTest {

    @Test
    fun directionsOpenGoogleMapsAtTheExactSpot() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=18.5823,73.8845",
            ExternalLinks.directions(18.5823, 73.8845),
        )
    }

    @Test
    fun directionsUseADotForDecimalsWhateverThePhonesLanguage() {
        // Some languages write 18,5823; a comma would split latitude from longitude wrongly
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=-12.5,0.25",
            ExternalLinks.directions(-12.5, 0.25),
        )
    }

    @Test
    fun phoneNumbersAreCleanedForTheDialer() {
        assertEquals("tel:+919876543210", ExternalLinks.phoneCall("+91 98765-43210"))
        assertEquals("tel:02026123456", ExternalLinks.phoneCall("(020) 2612 3456"))
    }

    @Test
    fun somethingThatIsNotAPhoneNumberIsNotDialled() {
        assertNull(ExternalLinks.phoneCall(""))
        assertNull(ExternalLinks.phoneCall("call me"))
        assertNull(ExternalLinks.phoneCall("12"))
    }
}
