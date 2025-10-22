package org.onereed.helios.sun

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.onereed.helios.common.Place
import org.shredzone.commons.suncalc.SunTimes

/** Tests for [SunEvent]. */
class SunEventTest {

  @Test
  fun testSunEvent() {
    val place = Place(34.0, -118.0, 25.0)
    val instant = Instant.parse("2025-10-19T22:34:18Z")
    val sunTimes =
      SunTimes.compute()
        .at(place.latDeg, place.lonDeg)
        .elevation(place.altitudeMeters)
        .on(instant)
        .execute()

    val sunEvent = SunEvent.from(SunEventType.SET, sunTimes, place)

    assertThat(sunEvent?.sunEventType).isEqualTo(SunEventType.SET)
    assertThat(sunEvent?.instant).isEqualTo(Instant.parse("2025-10-20T01:13:31Z"))
    assertThat(sunEvent?.azimuthDeg).isWithin(1.0).of(258.0)
    assertThat(sunEvent?.weakId).isEqualTo(0x68F58002)
  }
}
