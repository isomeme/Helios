package org.onereed.helios.sun

import java.util.EnumMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.common.arc
import org.shredzone.commons.suncalc.SunPosition
import timber.log.Timber

@OptIn(ExperimentalTime::class)
data class SunCompass(
  val sunAzimuth: Double,
  val isSunClockwise: Boolean,
  val events: EnumMap<SunEventType, Event>,
) {
  data class Event(val instant: Instant, val azimuth: Double) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
      return instant.compareTo(other.instant)
    }
  }

  companion object {

    /** The (short) time interval over which sun movement direction is determined. */
    private val DELTA_TIME = 1.minutes

    fun compute(sunTimeSeries: SunTimeSeries): SunCompass {
      Timber.d("compute start")

      val placeTime = sunTimeSeries.placeTime

      // Calculate current sun azimuth and movement direction.

      val sunAzimuth = placeTime.computeSunAzimuth()
      val sunAzimuthSoon =
        placeTime.copy(instant = placeTime.instant + DELTA_TIME).computeSunAzimuth()
      val deltaAzimuth = arc(sunAzimuth, sunAzimuthSoon)
      val isSunClockwise = deltaAzimuth >= 0

      /*
       * sunTimeSeries.events will typically contain 5 events in time order, 1 in the past and 4 in
       * the future, with the first and last events having the same SunEventType. For the compass
       * view, we only need one of each type, using the earlier version if there is a type
       * collision. The associate function keeps the last value for a duplicated key, so we reverse
       * the time ordering of the list to favor earlier times in key collisions.
       */

      val events =
        sunTimeSeries.events.reversed().associate {
          it.sunEventType to
            Event(it.instant, placeTime.copy(instant = it.instant).computeSunAzimuth())
        }

      return SunCompass(sunAzimuth, isSunClockwise, EnumMap(events))
    }

    private fun PlaceTime.computeSunAzimuth(): Double =
      SunPosition.compute()
        .at(lat, lon)
        .elevation(alt)
        .on(instant.toJavaInstant())
        .execute()
        .azimuth
  }
}
