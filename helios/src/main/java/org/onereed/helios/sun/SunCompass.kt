package org.onereed.helios.sun

import java.util.EnumMap
import timber.log.Timber

data class SunCompass(
  val sunAzimuthInfo: SunAzimuthInfo,
  val eventAzimuths: EnumMap<SunEventType, Double>,
) {
  companion object {

    fun compute(sunTimeSeries: SunTimeSeries): SunCompass {
      Timber.d("compute start")

      val placeTime = sunTimeSeries.placeTime
      val sunAzimuthInfo = SunAzimuthInfo.from(placeTime)

      val eventAzimuths =
        sunTimeSeries.events
          .reversed() // We want the earlier instance in key collisions.
          .associate { it.sunEventType to placeTime.atInstant(it.instant).computeSunAzimuth() }

      return SunCompass(sunAzimuthInfo, EnumMap(eventAzimuths))
    }
  }
}
