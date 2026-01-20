package org.onereed.helios.sun

import com.google.common.truth.Truth.assertWithMessage
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.junit.Rule
import org.junit.Test
import org.onereed.helios.datasource.PlaceTime
import org.onereed.helios.datasource.testing.santaMonica
import org.onereed.helios.util.TimberConsoleRule

/**
 * This test is intended to be modified as needed to find anomalous behaviors in [SunTimeSeries].
 */
@OptIn(ExperimentalTime::class)
class SunTimeSeriesSweepTest {

  @get:Rule val timberConsoleRule = TimberConsoleRule()

  @Test
  fun sweep() {
    var time = T0

    while (time < T1) {
      val placeTime = PlaceTime(santaMonica, time)
      val events = SunTimeSeries.create(placeTime).events

      assertWithMessage("placeTime=$placeTime").that(events).hasSize(5)

      time += DT
    }
  }

  companion object {

    private val T0 = Instant.parse("2025-12-17T20:00:00Z")
    private val T1 = Instant.parse("2025-12-18T08:00:00Z")
    private val DT = 100.milliseconds
  }
}
