package org.onereed.helios.sun

import androidx.compose.runtime.Immutable
import java.util.EnumMap
import kotlin.comparisons.reverseOrder
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import org.onereed.helios.common.ang
import org.onereed.helios.common.arc
import org.onereed.helios.datasource.PlaceTime
import org.shredzone.commons.suncalc.SunPosition

@OptIn(ExperimentalTime::class)
@Immutable
data class SunCompass(
  val sunAzimuth: Double,
  val isSunClockwise: Boolean,
  val events: EnumMap<SunEventType, Event>,
  val noonNadirOverlap: SunEventType?,
  val isValid: Boolean,
) {
  data class Event(val sunEventType: SunEventType, val time: Instant, val azimuth: Double) :
    Comparable<Event> {
    override fun compareTo(other: Event): Int {
      return time.compareTo(other.time)
    }
  }

  companion object {

    /** The (short) time interval over which sun movement direction is determined. */
    private val DELTA_TIME = 1.minutes

    fun compute(sunTimeSeries: SunTimeSeries): SunCompass {
      if (!sunTimeSeries.isValid)
        return SunCompass(
          sunAzimuth = 0.0,
          isSunClockwise = true,
          events = EnumMap(SunEventType::class.java),
          noonNadirOverlap = null,
          isValid = false,
        )

      val placeTime = sunTimeSeries.placeTime

      // Calculate current sun azimuth and movement direction.

      val sunAzimuth = placeTime.computeSunAzimuth()
      val sunAzimuthSoon = placeTime.copy(time = placeTime.time + DELTA_TIME).computeSunAzimuth()
      val deltaAzimuth = arc(sunAzimuth, sunAzimuthSoon)
      val isSunClockwise = deltaAzimuth >= 0

      val events =
        sunTimeSeries.events
          .map {
            Event(it.sunEventType, it.time, placeTime.copy(time = it.time).computeSunAzimuth())
          }
          .sortedWith(reverseOrder()) // Reverse time order (latest first)

      /*
       * sunTimeSeries.events will typically contain 5 events in time order, 1 in the past and 4 in
       * the future, with the first and last events having the same SunEventType. For the compass
       * view, we only need one of each type, using the earlier version if there is a type
       * collision. This keeps events from jumping around right after the sun passes them. The
       * associateBy function keeps the last value for a duplicated key, so we reverse the time
       * ordering of the list above to favor earlier times in key collisions.
       */

      val eventMap = EnumMap(events.associateBy(Event::sunEventType))
      val noonNadirOverlap = findNoonNadirOverlap(eventMap)

      return SunCompass(
        sunAzimuth = sunAzimuth,
        isSunClockwise = isSunClockwise,
        events = eventMap,
        noonNadirOverlap = noonNadirOverlap,
        isValid = true,
      )
    }

    private fun PlaceTime.computeSunAzimuth(): Double =
      SunPosition.compute()
        .at(place.lat, place.lon)
        .elevation(place.alt)
        .on(time.toJavaInstant())
        .execute()
        .azimuth

    /**
     * In the tropics, for part of each year the sun is further from the equator than the local
     * latitude. During this time the direction to the sun is always either north or south of the
     * east-west line, and noon and nadir both occur at either due north or due south. We detect
     * this and report which of the pair occurs later so that our display can indicate this
     * visually.
     */
    private fun findNoonNadirOverlap(eventMap: Map<SunEventType, Event>): SunEventType? {
      val noon = eventMap[SunEventType.NOON] ?: return null
      val nadir = eventMap[SunEventType.NADIR] ?: return null

      return if (ang(noon.azimuth, nadir.azimuth) < 10.0)
        maxOf(noon, nadir).sunEventType // Time comparison
      else null
    }
  }
}
