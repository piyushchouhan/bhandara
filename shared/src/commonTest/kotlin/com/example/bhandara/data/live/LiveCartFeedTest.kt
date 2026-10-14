package com.example.bhandara.data.live

import com.example.bhandara.ui.home.kmPerHour
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Following a moving cart live, the same on Android and iOS: positions come through as the vendor moves, and a
 * dropped connection is picked up again after a pause that grows while it keeps failing. The connection is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LiveCartFeedTest {

    private fun message(lat: Double, lng: Double, speed: Double = 0.0) =
        """{"shopId":7,"ownerUid":"vendor-1","lat":$lat,"lng":$lng,"speed":$speed}"""

    @Test
    fun positionsComeThroughAsTheyAreSentAndUnreadableOnesAreSkipped() = runTest {
        val topics = mutableListOf<String>()
        val feed = LiveCartFeed(subscribe = { topic ->
            topics += topic
            flowOf(message(18.58, 73.88), "not json", message(18.59, 73.89, speed = 1.5))
        })

        val positions = feed.positions(7).take(2).toList()

        assertEquals(listOf("/topic/cart/7"), topics)
        assertEquals(listOf(18.58 to 73.88, 18.59 to 73.89), positions.map { it.lat to it.lng })
        assertEquals(1.5, positions[1].speed)
    }

    @Test
    fun aDroppedConnectionIsPickedUpAgainAfterAPause() = runTest {
        var subscriptions = 0
        val connected = mutableListOf<Boolean>()
        val feed = LiveCartFeed(
            subscribe = {
                subscriptions++
                // The first connection sends one position and drops; the second keeps going
                if (subscriptions == 1) flowOf(message(18.58, 73.88)) else flowOf(message(18.60, 73.90))
            },
            pauseMillis = { 1_000L },
        )

        val positions = feed.positions(7) { connected += it }.take(2).toList()

        assertEquals(2, subscriptions)
        assertEquals(listOf(18.58, 18.60), positions.map { it.lat })
        assertEquals(listOf(true, false, true), connected)
        assertEquals(1_000L, currentTime, "waited before reconnecting")
    }

    @Test
    fun whileConnectingKeepsFailingThePauseGrows() = runTest {
        val pauses = mutableListOf<Int>()
        var attempts = 0
        val feed = LiveCartFeed(
            subscribe = {
                attempts++
                if (attempts <= 3) throw IllegalStateException("no network") else flowOf(message(18.58, 73.88))
            },
            pauseMillis = { failures -> pauses += failures; 100L * failures },
        )

        feed.positions(7).take(1).toList()

        assertEquals(listOf(1, 2, 3), pauses)
        assertEquals(600L, currentTime)
    }

    @Test
    fun afterAGoodConnectionThePauseStartsSmallAgain() = runTest {
        val pauses = mutableListOf<Int>()
        var attempts = 0
        val feed = LiveCartFeed(
            subscribe = {
                attempts++
                when (attempts) {
                    1 -> throw IllegalStateException("no network")
                    2 -> flowOf(message(18.58, 73.88))
                    else -> flow<String> { emit(message(18.60, 73.90)) }
                }
            },
            pauseMillis = { failures -> pauses += failures; 10L },
        )

        feed.positions(7).take(2).toList()

        assertEquals(listOf(1, 1), pauses, "the drop after a good connection counts as the first failure")
    }

    @Test
    fun theRealPausesGoFromOneSecondUpToHalfAMinute() {
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L, 30_000L), (1..7).map(::reconnectPauseMillis))
    }

    @Test
    fun speedReadsInKilometresPerHour() {
        assertEquals("4.5", kmPerHour(1.25))
        assertEquals("0.0", kmPerHour(0.0))
        assertEquals("21.6", kmPerHour(6.0))
    }
}
