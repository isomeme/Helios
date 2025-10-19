package org.onereed.helios.sun

import org.onereed.helios.common.DirectionUtil.arc
import org.onereed.helios.common.Place
import java.time.Duration
import java.time.Instant

/** Sun azimuth and azimuth movement direction (clockwise or counterclockwise). */
data class SunAzimuthInfo(val azimuthDeg: Double, val isClockwise: Boolean) {

  companion object {

    private val DELTA_TIME: Duration = Duration.ofMinutes(1L)

    fun from(place: Place, instant: Instant): SunAzimuthInfo {
      val parameters = place.asPositionParameters()
      val azimuthNow = parameters.on(instant).execute().azimuth
      val soon = instant.plus(DELTA_TIME)
      val azimuthSoon = parameters.on(soon).execute().azimuth
      val deltaAzimuth = arc(azimuthNow, azimuthSoon)

      return SunAzimuthInfo(azimuthNow, deltaAzimuth >= 0.0)
    }
  }
}
