package org.onereed.helios.sun

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes

/** Tests exercising the `SunCalc` library to confirm behaviors. */
class SunCalcTest {

  @Test
  fun noonNear180_good() {
    val where = doubleArrayOf(34.0, -118.5)
    val instant = Instant.parse("2020-06-16T04:11:00Z")
    val sunTimes = SunTimes.compute().at(where).on(instant).execute()
    val noon = sunTimes.noon
    assertThat(noon).isNotNull()

    val sunPosition = SunPosition.compute().at(where).on(noon).execute()
    val noonAzimuth = sunPosition.azimuth

    assertThat(noonAzimuth).isWithin(0.1).of(180.0)
  }
}
