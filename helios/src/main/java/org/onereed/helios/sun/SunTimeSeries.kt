package org.onereed.helios.sun

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaDuration
import kotlin.time.toJavaInstant
import org.onereed.helios.datasource.PlaceTime
import org.shredzone.commons.suncalc.SunTimes

@OptIn(ExperimentalTime::class)
@Immutable
data class SunTimeSeries(
  val placeTime: PlaceTime,
  val events: List<Event>,
  val isValid: Boolean = true,
) {
  @Immutable
  data class Event(val sunEventType: SunEventType, val time: Instant) : Comparable<Event> {
    override fun compareTo(other: Event) =
      compareValuesBy(this, other, { it.time }, { it.sunEventType })
  }

  companion object {

    fun create(placeTime: PlaceTime): SunTimeSeries {
      if (!placeTime.isValid) return INVALID

      val futureSunTimes = placeTime.computeSunTimes(FUTURE_LIMIT)
      val futureEvents = toEvents(futureSunTimes)
      val nextEvent = futureEvents.first()

      val earlierPlaceTime = placeTime.copy(time = placeTime.time - PRECEDING_OFFSET)
      val pastSunTimes = earlierPlaceTime.computeSunTimes(PRECEDING_LIMIT)
      val pastEvents = toEvents(pastSunTimes)
      val lastEvent =
        pastEvents
          .filter { it.sunEventType != nextEvent.sunEventType }
          .last { it.time < placeTime.time }

      val allEvents = listOf(lastEvent) + futureEvents

      // Downstream logic relies on there being at least two events (noon and midnight), so if for
      // some reason we have fewer, mark the data as being invalid.

      return if (allEvents.size < 2) INVALID else SunTimeSeries(placeTime, allEvents)
    }

    private fun PlaceTime.computeSunTimes(limit: Duration): SunTimes =
      SunTimes.compute()
        .at(place.lat, place.lon)
        .elevation(place.alt)
        .on(time.toJavaInstant())
        .limit(limit.toJavaDuration())
        .execute()

    private fun toEvents(sunTimes: SunTimes): List<Event> {
      return SunEventType.entries
        .map { Pair(it, it.timeOf(sunTimes)) }
        .filter { it.second != null }
        .map { Event(it.first, it.second!!) }
        .sorted()
    }

    private val INVALID = SunTimeSeries(PlaceTime.INVALID, emptyList(), false)

    /**
     * We search farther than one day ahead because e.g. around the spring equinox successive
     * sunsets are more than 24 hours apart, so if we're right on top of one of them, a short window
     * could miss the next one.
     */
    private val FUTURE_LIMIT = 36.hours

    /**
     * We use this value combined with [PRECEDING_LIMIT] to include "preceding" events up to and
     * beyond the current instant, to avoid edge cases where an event happening near the time we're
     * checking falls out of both the preceding and upcoming events lists. We then remove duplicates
     * in the preceding events.
     */
    private val PRECEDING_OFFSET = 13.hours

    /** See [PRECEDING_OFFSET]. */
    private val PRECEDING_LIMIT = 14.hours
  }
}
