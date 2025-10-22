package org.onereed.helios.sun

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Before
import org.junit.Test
import org.onereed.helios.common.DirectionUtil.ang
import org.onereed.helios.common.Place
import timber.log.Timber
import timber.log.Timber.DebugTree

/** Tests for [SunInfo]. */
class SunInfoTest {

  @Before
  fun setup() {
    Timber.plant(DebugTree())
  }

  /**
   * Make sure we don't use a "preceding" event in the future. In this case, the same sunset appears
   * at the end of the old data and the beginning of the new data.
   */
  @Test
  fun testEventOverlap() {
    val instant = Instant.parse("2020-05-09T02:30:15Z")
    val place = Place(34.0, -118.5, 0.0, instant)
    val sunTimeSeries = SunTimeSeries.compute(place)
    val sunInfo = SunInfo.compute(sunTimeSeries)

    assertThat(sunInfo.sunAzimuthInfo.azimuthDeg).isWithin(DELTA).of(290.0)
    assertThat(sunInfo.sunAzimuthInfo.isClockwise).isTrue()
    assertThat(sunInfo.closestEventIndex).isEqualTo(1)
    assertThat(sunInfo.sunEvents).hasSize(5)

    confirmEvent(sunInfo.sunEvents[0], "2020-05-08T19:50:33Z", SunEventType.NOON, 180.0)
    confirmEvent(sunInfo.sunEvents[1], "2020-05-09T02:43:51Z", SunEventType.SET, 292.0)
    confirmEvent(sunInfo.sunEvents[2], "2020-05-09T07:50:18Z", SunEventType.NADIR, 0.0)
    confirmEvent(sunInfo.sunEvents[3], "2020-05-09T12:56:55Z", SunEventType.RISE, 68.0)
    confirmEvent(sunInfo.sunEvents[4], "2020-05-09T19:50:31Z", SunEventType.NOON, 180.0)
  }

  /**
   * Make sure that we don't miss an event that we're right on top of by excluding it from both
   * preceding and following sets.
   */
  @Test
  fun testEventGap() {
    val instant = Instant.parse("2020-05-18T02:50:50Z")
    val place = Place(34.0, -118.5, 0.0, instant)
    val sunTimeSeries = SunTimeSeries.compute(place)
    val sunInfo = SunInfo.compute(sunTimeSeries)

    assertThat(sunInfo.sunAzimuthInfo.azimuthDeg).isWithin(DELTA).of(294.5)
    assertThat(sunInfo.sunAzimuthInfo.isClockwise).isTrue()
    assertThat(sunInfo.closestEventIndex).isEqualTo(0)
    assertThat(sunInfo.sunEvents).hasSize(5)

    confirmEvent(sunInfo.sunEvents[0], "2020-05-18T02:50:36Z", SunEventType.SET, 294.5)
    confirmEvent(sunInfo.sunEvents[1], "2020-05-18T07:50:21Z", SunEventType.NADIR, 0.0)
    confirmEvent(sunInfo.sunEvents[2], "2020-05-18T12:50:06Z", SunEventType.RISE, 65.0)
    confirmEvent(sunInfo.sunEvents[3], "2020-05-18T19:50:33Z", SunEventType.NOON, 180.0)
    confirmEvent(sunInfo.sunEvents[4], "2020-05-19T02:51:19Z", SunEventType.SET, 295.0)
  }

  companion object {

    /** Tolerance for degree comparisons. */
    private const val DELTA = 0.5

    private fun confirmEvent(
      sunEvent: SunEvent,
      instantStr: String,
      sunEventType: SunEventType,
      azimuthDeg: Double,
    ) {
      assertThat(sunEvent.instant).isEqualTo(Instant.parse(instantStr))
      assertThat(sunEvent.sunEventType).isEqualTo(sunEventType)
      assertThat(ang(sunEvent.azimuthDeg, azimuthDeg)).isWithin(DELTA).of(0.0)
    }
  }
}
