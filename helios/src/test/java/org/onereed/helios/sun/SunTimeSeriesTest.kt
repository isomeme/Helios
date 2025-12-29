package org.onereed.helios.sun

import com.google.common.truth.Truth.assertThat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.junit.Test
import org.onereed.helios.datasource.PlaceTime
import org.onereed.helios.datasource.testing.santaMonica

/** Tests for [SunTimeSeries]. */
@OptIn(ExperimentalTime::class)
class SunTimeSeriesTest {

  /**
   * Make sure we don't use a "preceding" event in the future. In this case, the same sunset appears
   * at the end of the old data and the beginning of the new data.
   */
  @Test
  fun testEventOverlap() {
    val instant = Instant.parse("2020-05-09T02:30:15Z")
    val placeTime = PlaceTime(santaMonica, instant)
    val events = SunTimeSeries(placeTime).events

    assertThat(events).hasSize(5)

    confirmEvent(events[0], SunEventType.NOON, "2020-05-08T19:50:33Z")
    confirmEvent(events[1], SunEventType.SET, "2020-05-09T02:43:51Z")
    confirmEvent(events[2], SunEventType.NADIR, "2020-05-09T07:50:18Z")
    confirmEvent(events[3], SunEventType.RISE, "2020-05-09T12:56:55Z")
    confirmEvent(events[4], SunEventType.NOON, "2020-05-09T19:50:31Z")
  }

  /**
   * Make sure that we don't miss an event that we're right on top of by excluding it from both
   * preceding and following sets.
   */
  @Test
  fun testEventGap() {
    val instant = Instant.parse("2020-05-18T02:50:50Z")
    val placeTime = PlaceTime(santaMonica, instant)
    val events = SunTimeSeries(placeTime).events

    assertThat(events).hasSize(5)

    confirmEvent(events[0], SunEventType.SET, "2020-05-18T02:50:36Z")
    confirmEvent(events[1], SunEventType.NADIR, "2020-05-18T07:50:21Z")
    confirmEvent(events[2], SunEventType.RISE, "2020-05-18T12:50:06Z")
    confirmEvent(events[3], SunEventType.NOON, "2020-05-18T19:50:33Z")
    confirmEvent(events[4], SunEventType.SET, "2020-05-19T02:51:19Z")
  }

  companion object {

    private fun confirmEvent(
      event: SunTimeSeries.Event,
      sunEventType: SunEventType,
      instantStr: String,
    ) {
      assertThat(event.sunEventType).isEqualTo(sunEventType)
      assertThat(event.time).isEqualTo(Instant.parse(instantStr))
    }
  }
}
