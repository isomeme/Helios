package org.onereed.helios.sun

import com.google.common.truth.Truth.assertWithMessage
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.junit.Rule
import org.junit.Test
import org.onereed.helios.datasource.PlaceTime
import org.onereed.helios.util.TimberRule

/**
 * This test is intended to be modified as needed to find anomalous behaviors in [SunTimeSeries].
 */
@OptIn(ExperimentalTime::class)
class SunTimeSeriesSweepTest {

  @get:Rule val timberRule = TimberRule()

  @Test
  fun sweep() {
    var time = T0

    while (time < T1) {
      val placeTime = PlaceTime(LAT, LON, ALT, time)
      val events = SunTimeSeries(placeTime).events

      assertWithMessage("placeTime=$placeTime").that(events).hasSize(5)

      time += DT
    }
  }

  companion object {

    // Lat and lon are for Playa Vista, California, USA.

    private const val LAT = 33.978
    private const val LON = -118.407
    private const val ALT = 0.0

    private val T0 = Instant.parse("2025-12-17T20:00:00Z")
    private val T1 = Instant.parse("2025-12-18T08:00:00Z")
    private val DT = 100.milliseconds
  }
}
