package org.onereed.helios.sun

import org.onereed.helios.common.DirectionUtil.arc
import org.onereed.helios.common.Place
import java.time.Duration

/** Sun azimuth and azimuth movement direction (clockwise or counterclockwise). */
data class SunAzimuthInfo(val azimuthDeg: Double, val isClockwise: Boolean) {

  companion object {

    private val DELTA_TIME: Duration = Duration.ofMinutes(1L)

    fun from(place: Place): SunAzimuthInfo {
      val parameters = place.asPositionParameters()
      val azimuthNow = parameters.on(place.instant).execute().azimuth
      val soon = place.instant.plus(DELTA_TIME)
      val azimuthSoon = parameters.on(soon).execute().azimuth
      val deltaAzimuth = arc(azimuthNow, azimuthSoon)

      return SunAzimuthInfo(azimuthNow, deltaAzimuth >= 0.0)
    }
  }
}
