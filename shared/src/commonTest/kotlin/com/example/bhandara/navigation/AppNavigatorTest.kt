package com.example.bhandara.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Moving between screens, the same on Android and iOS */
class AppNavigatorTest {

    @Test
    fun theAppStartsOnHome() {
        val navigator = AppNavigator()

        assertEquals(Route.Home, navigator.current)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun goingBackReturnsToTheScreenBefore() {
        val navigator = AppNavigator()
        navigator.navigate(Route.Hungry)
        navigator.navigate(Route.ShopDetails("5"))

        assertTrue(navigator.back())
        assertEquals(Route.Hungry, navigator.current)
        assertTrue(navigator.back())
        assertEquals(Route.Home, navigator.current)
    }

    @Test
    fun goingBackFromHomeIsLeftToThePlatform() {
        val navigator = AppNavigator()

        assertFalse(navigator.back())
        assertEquals(listOf<Route>(Route.Home), navigator.backStack)
    }

    @Test
    fun aDoubleTapOpensTheScreenOnce() {
        val navigator = AppNavigator()

        navigator.navigate(Route.Hungry)
        navigator.navigate(Route.Hungry)

        assertEquals(listOf(Route.Home, Route.Hungry), navigator.backStack)
    }

    @Test
    fun theSameKindOfScreenCanBeOpenedForDifferentItems() {
        val navigator = AppNavigator()

        navigator.navigate(Route.ShopDetails("5"))
        navigator.navigate(Route.ShopDetails("6"))

        assertEquals(Route.ShopDetails("6"), navigator.current)
        assertEquals(3, navigator.backStack.size)
    }

    @Test
    fun theOpenScreensSurviveRotationWithTheirDetails() {
        val navigator = AppNavigator()
        navigator.navigate(Route.Hungry)
        navigator.navigate(Route.ShopDetails("5"))
        navigator.navigate(Route.ClaimShop("5", "Sharma Chaat"))

        val restored = AppNavigator.restore(navigator.save())

        assertEquals(navigator.backStack, restored.backStack)
    }

    @Test
    fun aSavedScreenTheAppNoLongerKnowsIsSkipped() {
        val restored = AppNavigator.restore(listOf("""{"type":"com.example.bhandara.navigation.Route.Home"}""", "garbage"))

        assertEquals(listOf<Route>(Route.Home), restored.backStack)
        assertEquals(listOf<Route>(Route.Home), AppNavigator.restore(emptyList()).backStack)
    }

    @Test
    fun aScreenCanBeReplacedSoBackSkipsIt() {
        // After posting a feast, its page replaces the form: back goes home, not to the form
        val navigator = AppNavigator()
        navigator.navigate(Route.ReportFeast)

        navigator.replace(Route.FeastDetails("77"))

        assertEquals(listOf(Route.Home, Route.FeastDetails("77")), navigator.backStack)
        navigator.back()
        assertEquals(Route.Home, navigator.current)
    }

    @Test
    fun homeIsNeverReplaced() {
        val navigator = AppNavigator()

        navigator.replace(Route.Hungry)

        assertEquals(listOf(Route.Home, Route.Hungry), navigator.backStack)
    }

    @Test
    fun goingBackToHomeClosesEverythingAboveIt() {
        val navigator = AppNavigator()
        navigator.navigate(Route.Profile)
        navigator.navigate(Route.MyReviews)

        navigator.backToHome()

        assertEquals(listOf(Route.Home), navigator.backStack)
        // Already home: nothing to close
        navigator.backToHome()
        assertEquals(listOf(Route.Home), navigator.backStack)
    }
}
