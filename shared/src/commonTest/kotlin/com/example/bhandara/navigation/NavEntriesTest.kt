package com.example.bhandara.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Each open screen keeps its view models until it's closed, on Android and iOS */
class NavEntriesTest {

    private class Probe : ViewModel() {
        var cleared = false
        override fun onCleared() {
            cleared = true
        }
    }

    private fun NavEntryStores.probe(key: String): Probe =
        ViewModelProvider.create(storeFor(key), viewModelFactory { initializer { Probe() } })[Probe::class]

    @Test
    fun eachOpenScreenHasItsOwnKey() {
        val navigator = AppNavigator()
        navigator.navigate(Route.ShopDetails("5"))
        navigator.navigate(Route.ClaimShop("5", "Sharma Chaat"))
        navigator.back()
        navigator.navigate(Route.ShopDetails("6"))

        val keys = navigator.entryKeys()

        assertEquals(3, keys.size)
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys[1].contains("5") && keys[2].contains("6"))
    }

    @Test
    fun aScreenKeepsItsKeyWhileOthersOpenAndCloseAboveIt() {
        val navigator = AppNavigator()
        navigator.navigate(Route.Hungry)
        val mapKey = navigator.entryKeys().last()

        navigator.navigate(Route.ShopDetails("5"))
        navigator.back()

        assertEquals(mapKey, navigator.entryKeys().last())
    }

    @Test
    fun goingBackToAScreenFindsItsViewModels() {
        val stores = NavEntryStores()
        val map = stores.probe("1:Hungry")

        stores.keepOnly(listOf("0:Home", "1:Hungry", "2:ShopDetails"))

        assertSame(map, stores.probe("1:Hungry"))
        assertFalse(map.cleared)
    }

    @Test
    fun closingAScreenClearsItsViewModels() {
        val stores = NavEntryStores()
        val details = stores.probe("2:ShopDetails")

        stores.keepOnly(listOf("0:Home", "1:Hungry"))

        assertTrue(details.cleared)
        assertNotEquals<Any>(details, stores.probe("2:ShopDetails"))
    }

    @Test
    fun onlyASwipeFromTheLeftEdgeMostlyRightwardsGoesBack() {
        assertTrue(isBackSwipe(startX = 10f, dx = 100f, dy = 20f, edgeWidth = 24f, distance = 80f))
        // Starts away from the edge (e.g. scrolling a photo strip)
        assertFalse(isBackSwipe(startX = 100f, dx = 100f, dy = 0f, edgeWidth = 24f, distance = 80f))
        // Too short
        assertFalse(isBackSwipe(startX = 10f, dx = 50f, dy = 0f, edgeWidth = 24f, distance = 80f))
        // Mostly vertical (scrolling the page)
        assertFalse(isBackSwipe(startX = 10f, dx = 90f, dy = 80f, edgeWidth = 24f, distance = 80f))
    }
}
