package org.onereed.helios.sun

import timber.log.Timber
import java.time.Instant
import java.util.EnumMap

data class SunCompass(
  val sunAzimuthInfo: SunAzimuthInfo,
  val events: EnumMap<SunEventType, Event>,
) {
  data class Event(val instant: Instant, val azimuth: Double) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
      return instant.compareTo(other.instant)
    }
  }

  companion object {

    fun compute(sunTimeSeries: SunTimeSeries): SunCompass {
      Timber.d("compute start")

      val placeTime = sunTimeSeries.placeTime
      val sunAzimuthInfo = SunAzimuthInfo.from(placeTime)

      /*
       * sunTimeSeries.events will typically contain 5 events in time order, 1 in the past and 4 in
       * the future, with the first and last events having the same SunEventType. For the compass
       * view, we only need one of each type, using the earlier version if there is a type
       * collision. The associate function keeps the last value for a duplicated key, so we reverse
       * the time ordering of the list to favor earlier times in key collisions.
       */

      val events =
        sunTimeSeries.events.reversed().associate {
          it.sunEventType to Event(it.instant, placeTime.atInstant(it.instant).computeSunAzimuth())
        }

      return SunCompass(sunAzimuthInfo, EnumMap(events))
    }
  }
}
