package org.onereed.helios.sun

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.onereed.helios.common.PlaceTime
import org.shredzone.commons.suncalc.SunTimes

/** Tests for [SunEvent]. */
class SunEventTest {

  @Test
  fun testSunEvent() {
    val instant = Instant.parse("2025-10-19T22:34:18Z")
    val placeTime = PlaceTime(34.0, -118.0, 25.0, instant)
    val sunTimes =
      SunTimes.compute()
        .at(placeTime.latDeg, placeTime.lonDeg)
        .elevation(placeTime.altMeters)
        .on(placeTime.instant)
        .execute()

    val sunEvent = SunEvent.from(SunEventType.SET, sunTimes, placeTime)

    assertThat(sunEvent?.sunEventType).isEqualTo(SunEventType.SET)
    assertThat(sunEvent?.instant).isEqualTo(Instant.parse("2025-10-20T01:13:31Z"))
    assertThat(sunEvent?.azimuthDeg).isWithin(1.0).of(258.0)
    assertThat(sunEvent?.weakId).isEqualTo(0x68F58002)
  }
}
