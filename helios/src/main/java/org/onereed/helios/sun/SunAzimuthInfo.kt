package org.onereed.helios.sun

import org.onereed.helios.common.DirectionUtil.arc
import org.onereed.helios.common.PlaceTime
import java.time.Duration

/** Sun azimuth and azimuth movement direction (clockwise or counterclockwise). */
data class SunAzimuthInfo(val deg: Double, val isClockwise: Boolean) {

  companion object {

    private val DELTA_TIME: Duration = Duration.ofMinutes(1L)

    fun from(placeTime: PlaceTime): SunAzimuthInfo {
      val azimuthNow = placeTime.computeSunAzimuth()
      val soon = placeTime.plusDuration(DELTA_TIME)
      val azimuthSoon = soon.computeSunAzimuth()
      val deltaAzimuth = arc(azimuthNow, azimuthSoon)

      return SunAzimuthInfo(azimuthNow, deltaAzimuth >= 0.0)
    }
  }
}
