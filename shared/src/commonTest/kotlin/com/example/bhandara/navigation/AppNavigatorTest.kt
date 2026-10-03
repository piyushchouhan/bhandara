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
        navigator.navigate(Route.ShopsMap)
        navigator.navigate(Route.ShopDetails("5"))

        assertTrue(navigator.back())
        assertEquals(Route.ShopsMap, navigator.current)
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

        navigator.navigate(Route.ShopsMap)
        navigator.navigate(Route.ShopsMap)

        assertEquals(listOf(Route.Home, Route.ShopsMap), navigator.backStack)
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
        navigator.navigate(Route.ShopsMap)
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
}
