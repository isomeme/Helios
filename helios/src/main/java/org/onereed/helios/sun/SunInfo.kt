package org.onereed.helios.sun

import java.time.Duration
import java.time.Instant
import timber.log.Timber

data class SunInfo(
  val sunAzimuthInfo: SunAzimuthInfo,
  val closestEventIndex: Int,
  val sunEvents: List<SunEvent>,
) {
  companion object {

    fun compute(sunTimeSeries: SunTimeSeries): SunInfo {
      Timber.d("compute start")

      val parameters = sunTimeSeries.place.asPositionParameters()
      val sunEvents =
        sunTimeSeries.events.map {
          SunEvent(it.sunEventType, it.instant, parameters.on(it.instant).execute().azimuth)
        }

      val sunAzimuthInfo = SunAzimuthInfo.from(sunTimeSeries.place)
      val closestEventIndex =
        getClosestEventIndex(sunTimeSeries.place.instant, sunEvents[0], sunEvents[1])

      return SunInfo(sunAzimuthInfo, closestEventIndex, sunEvents)
    }

    private fun getClosestEventIndex(
      instant: Instant,
      lastEvent: SunEvent,
      nextEvent: SunEvent,
    ): Int =
      if (
        Duration.between(lastEvent.instant, instant) < Duration.between(instant, nextEvent.instant)
      ) {
        0
      } else {
        1
      }
  }
}
