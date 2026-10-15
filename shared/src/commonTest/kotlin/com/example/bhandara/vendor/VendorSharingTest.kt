package com.example.bhandara.vendor

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.CartLocationUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A vendor's cart location, shared the same way on Android and iOS: every fix while it moves, cheap location once
 * it has stood still a while, a presence update when nothing else was sent, and off the map at once when stopped.
 * The connection is a fake; time is the test's.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VendorSharingTest {

    // ==================== Fakes ====================

    private class FakeSender : CartLocationSender {
        val sent = mutableListOf<CartLocationUpdate>()
        val offline = mutableListOf<CartLocationUpdate>()
        var closed = false
        var failing = false

        override suspend fun send(update: CartLocationUpdate) {
            if (failing) error("connection lost")
            sent += update
        }

        override suspend fun sendOffline(update: CartLocationUpdate) {
            offline += update
        }

        override suspend fun close() {
            closed = true
        }
    }

    private val sender = FakeSender()
    private val powerChanges = mutableListOf<VendorPower>()

    private fun TestScope.sharing() = VendorSharing(7, "vendor-1", sender, backgroundScope, { currentTime }, { powerChanges += it })
        .also { it.start() }

    // 0.0001° of latitude is about 11 m
    private val stall = GeoLocation(18.5823, 73.8845)
    private fun metersNorth(meters: Double) = GeoLocation(stall.latitude + meters / 111_000.0, stall.longitude)

    private fun TestScope.fix(at: GeoLocation, accuracy: Double = 5.0, speed: Double? = null) = VendorFix(at, currentTime, accuracy, speed)

    // ==================== Tests ====================

    @Test
    fun whileTheCartMovesEveryFixIsSent() = runTest {
        val sharing = sharing()

        sharing.onFix(fix(stall, speed = 1.2))
        advanceTimeBy(4_000)
        sharing.onFix(fix(metersNorth(30.0), speed = 1.4))
        runCurrent()

        assertEquals(2, sender.sent.size)
        assertEquals(7L, sender.sent[0].shopId)
        assertEquals("vendor-1", sender.sent[0].ownerUid)
        assertEquals(1.4, sender.sent[1].speed)
        assertEquals(VendorPower.MOVING, sharing.power)
    }

    @Test
    fun aCartStandingStillForTwoMinutesSwitchesToCheapLocation() = runTest {
        val sharing = sharing()
        sharing.onFix(fix(stall))

        // Small wobbles within 20 m don't count as moving
        repeat(3) {
            advanceTimeBy(30_000)
            sharing.onFix(fix(metersNorth(8.0)))
        }
        assertEquals(VendorPower.MOVING, sharing.power, "90 seconds isn't long enough")

        advanceTimeBy(31_000)
        sharing.onFix(fix(metersNorth(5.0)))

        assertEquals(VendorPower.PARKED, sharing.power)
        assertEquals(listOf(VendorPower.PARKED), powerChanges)
    }

    @Test
    fun whileParkedOnlyClearMovementSwitchesAccurateLocationBackOn() = runTest {
        val sharing = sharing()
        sharing.onFix(fix(stall))
        // No new fix, but the presence check (every 45 s) notices it stood still: at 135 s, the first after 2 minutes
        advanceTimeBy(136_000)
        runCurrent()
        assertEquals(VendorPower.PARKED, sharing.power)
        val sentBefore = sender.sent.size

        // A cheap fix 50 m off but only accurate to 30 m: probably still there
        sharing.onFix(fix(metersNorth(50.0), accuracy = 30.0))
        runCurrent()
        assertEquals(VendorPower.PARKED, sharing.power)
        assertEquals(sentBefore, sender.sent.size)

        // 80 m off, accurate to 20 m: it's moving
        sharing.onFix(fix(metersNorth(80.0), accuracy = 20.0))
        runCurrent()

        assertEquals(VendorPower.MOVING, sharing.power)
        assertEquals(sentBefore + 1, sender.sent.size)
    }

    @Test
    fun whenNothingWasSentForAWhileThePositionIsSentAgain() = runTest {
        sharing().onFix(fix(stall, speed = 2.0))
        runCurrent()

        advanceTimeBy(45_001)
        runCurrent()

        assertEquals(2, sender.sent.size)
        assertEquals(sender.sent[0].lat, sender.sent[1].lat)
        assertEquals(0.0, sender.sent[1].speed, "a presence update isn't movement")
    }

    @Test
    fun aFailedSendDoesNotStopSharing() = runTest {
        val sharing = sharing()
        sender.failing = true
        sharing.onFix(fix(stall))
        runCurrent()

        sender.failing = false
        advanceTimeBy(4_000)
        sharing.onFix(fix(metersNorth(30.0)))
        runCurrent()

        assertTrue(sharing.isRunning)
        assertEquals(1, sender.sent.size)
    }

    @Test
    fun stoppingTakesTheCartOffTheMapAtItsLastSpotAndStopsSending() = runTest {
        val sharing = sharing()
        sharing.onFix(fix(metersNorth(30.0)))
        runCurrent()

        sharing.stop()
        sharing.onFix(fix(metersNorth(60.0)))
        advanceTimeBy(100_000)
        runCurrent()

        assertFalse(sharing.isRunning)
        assertEquals(1, sender.sent.size)
        assertEquals(sender.sent[0].lat, sender.offline.single().lat)
        assertTrue(sender.closed)
    }
}
