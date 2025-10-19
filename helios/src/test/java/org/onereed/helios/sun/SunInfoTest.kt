package org.onereed.helios.sun

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner.StrictStubs
import org.onereed.helios.common.Place
import org.onereed.helios.sun.SunInfo.Companion.compute
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.time.Instant

/** Tests for [SunInfo].  */
@RunWith(StrictStubs::class)
class SunInfoTest {

    @Before
    fun setup() {
        Timber.plant(DebugTree())
    }

    /**
     * Make sure we don't use a "preceding" event in the future. In this case, the same sunset
     * appears at the end of the old data and the beginning of the new data.
     */
    @Test
    fun testEventOverlap() {
        val instant = Instant.parse("2020-05-09T02:30:15Z")
        val sunInfo = compute(PLACE, instant)

        val expectedSunInfo = SunInfo(
            instant, SunAzimuthInfo(289.93357351191065, true), closestEventIndex = 1, listOf(
                event("2020-05-08T19:50:33Z", SunEvent.Type.NOON, 180.0486292899211),
                event("2020-05-09T02:43:51Z", SunEvent.Type.SET, 291.8290562827426),
                event("2020-05-09T07:50:18Z", SunEvent.Type.NADIR, 359.9482197950236),
                event("2020-05-09T12:56:55Z", SunEvent.Type.RISE, 68.05589238252242),
                event("2020-05-09T19:50:31Z", SunEvent.Type.NOON, 180.05583201838022)
            )
        )

        assertEquals(expectedSunInfo, sunInfo)
    }

    /**
     * Make sure that we don't miss an event that we're right on top of by excluding it from both
     * preceding and following sets.
     */
    @Test
    fun testEventGap() {
        val instant = Instant.parse("2020-05-18T02:50:50Z")
        val sunInfo = compute(PLACE, instant)

        val expectedSunInfo = SunInfo(
            instant, SunAzimuthInfo(294.5638405069754, true), closestEventIndex = 0, listOf(
                event("2020-05-18T02:50:35Z", SunEvent.Type.SET, 294.52853932175793),
                event("2020-05-18T07:50:21Z", SunEvent.Type.NADIR, 359.9604617342177),
                event("2020-05-18T12:50:06Z", SunEvent.Type.RISE, 65.35870558986005),
                event("2020-05-18T19:50:33Z", SunEvent.Type.NOON, 180.03983395176928),
                event("2020-05-19T02:51:19Z", SunEvent.Type.SET, 294.79749942898354)
            )
        )

        assertEquals(expectedSunInfo, sunInfo)
    }

    companion object {

        /** Santa Monica CA USA */
        private val PLACE = Place(34.0, -118.5, 0.0)

        private fun event(whenText: String, type: SunEvent.Type, azimuth: Double): SunEvent {
            val instant = Instant.parse(whenText)
            return SunEvent(type, instant, azimuth)
        }
    }
}
