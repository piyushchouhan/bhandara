package com.example.bhandara.data.location

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Finding where the user is, the same on Android and iOS, with the phone's location replaced by a fake */
class LocationServiceTest {

    private val now = 1_000_000_000L
    private val here = GeoLocation(18.5823, 73.8845)
    private val aMinuteAgo = TimedLocation(GeoLocation(18.5800, 73.8800), now - 60_000)

    private class FakePhone(
        var permitted: Boolean = true,
        var fresh: GeoLocation? = null,
        var freshTakesMillis: Long = 0,
        var freshFails: Boolean = false,
        var last: TimedLocation? = null,
    ) : DeviceLocation {
        var freshRequests = 0
        override fun hasPermission() = permitted
        override suspend fun current(): GeoLocation? {
            freshRequests++
            delay(freshTakesMillis)
            if (freshFails) throw IllegalStateException("Location services are off")
            return fresh
        }
        override suspend fun lastKnown() = last
    }

    private fun service(phone: FakePhone) = LocationService(phone, timeoutMillis = 10_000, nowMillis = { now })

    @Test
    fun withoutPermissionThePhoneIsNotAsked() = runTest {
        val phone = FakePhone(permitted = false, fresh = here)

        assertEquals(LocationResult.PermissionDenied, service(phone).locate())
        assertEquals(0, phone.freshRequests)
    }

    @Test
    fun aFreshFixIsUsed() = runTest {
        assertEquals(LocationResult.Found(here), service(FakePhone(fresh = here, last = aMinuteAgo)).locate())
    }

    @Test
    fun whenAFreshFixTakesTooLongARecentOneIsUsed() = runTest {
        val phone = FakePhone(fresh = here, freshTakesMillis = 30_000, last = aMinuteAgo)

        assertEquals(LocationResult.Found(aMinuteAgo.location), service(phone).locate())
    }

    @Test
    fun whenAFreshFixFailsARecentOneIsUsed() = runTest {
        assertEquals(LocationResult.Found(aMinuteAgo.location), service(FakePhone(fresh = null, last = aMinuteAgo)).locate())
        assertEquals(
            LocationResult.Found(aMinuteAgo.location),
            service(FakePhone(freshFails = true, last = aMinuteAgo)).locate(),
        )
    }

    @Test
    fun aLocationFromLongAgoIsNotTrusted() = runTest {
        val anHourAgo = TimedLocation(GeoLocation(19.07, 72.87), now - 60 * 60_000)

        assertEquals(LocationResult.Unavailable, service(FakePhone(fresh = null, last = anHourAgo)).locate())
    }

    @Test
    fun withNoLocationAtAllItSaysSo() = runTest {
        assertEquals(LocationResult.Unavailable, service(FakePhone(fresh = null, last = null)).locate())
    }
}
