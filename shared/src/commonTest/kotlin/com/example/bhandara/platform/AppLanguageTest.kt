package com.example.bhandara.platform

import kotlin.test.Test
import kotlin.test.assertEquals

/** Which of the app's languages a phone's locale means */
class AppLanguageTest {

    @Test
    fun aTranslatedLanguageIsUsedWhateverTheRegion() {
        assertEquals("hi", appLanguageFor("hi-IN"))
        assertEquals("mr", appLanguageFor("mr"))
        assertEquals("en", appLanguageFor("en-GB"))
        assertEquals("hi", appLanguageFor("HI_in"))
    }

    @Test
    fun otherLanguagesFallBackToEnglish() {
        assertEquals("en", appLanguageFor("ta-IN"))
        assertEquals("en", appLanguageFor(""))
        assertEquals("en", appLanguageFor(null))
    }
}
