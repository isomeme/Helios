package org.onereed.helios.sun

import java.time.Duration
import java.time.Instant
import org.onereed.helios.common.Place
import org.onereed.helios.sun.SunTimeSeries.Companion.PRECEDING_LIMIT
import org.onereed.helios.sun.SunTimeSeries.Companion.PRECEDING_OFFSET
import org.shredzone.commons.suncalc.SunTimes
import timber.log.Timber

data class SunTimeSeries(val events: List<Event>, val place: Place) {
  data class Event(val sunEventType: SunEventType, val instant: Instant) : Comparable<Event> {

    override fun compareTo(other: Event) =
      compareValuesBy(this, other, { it.instant }, { it.sunEventType })
  }

  companion object {

    /**
     * We search farther than one day ahead because e.g. around the spring equinox successive
     * sunsets are more than 24 hours apart, so if we're right on top of one of them, a short window
     * could miss the next one.
     */
    private val FUTURE_LIMIT = Duration.ofHours(36L)

    /**
     * We use this value combined with [PRECEDING_LIMIT] to include "preceding" events up to and
     * beyond the current instant, to avoid edge cases where an event happening near the time we're
     * checking falls out of both the preceding and upcoming events lists. We then remove duplicates
     * in the preceding events.
     */
    private val PRECEDING_OFFSET = Duration.ofHours(13L)

    /** See [PRECEDING_OFFSET]. */
    private val PRECEDING_LIMIT = Duration.ofHours(14L)

    fun compute(place: Place): SunTimeSeries {
      Timber.d("compute place=$place")

      val parameters = place.asTimesParameters()

      val futureSunTimes = parameters.on(place.instant).limit(FUTURE_LIMIT).execute()
      val futureEvents = toEvents(futureSunTimes)
      val nextEvent = futureEvents.first()

      val earlierTime = place.instant.minus(PRECEDING_OFFSET)
      val pastSunTimes = parameters.on(earlierTime).limit(PRECEDING_LIMIT).execute()
      val pastEvents = toEvents(pastSunTimes)
      val lastEvent =
        pastEvents
          .filter { it.sunEventType != nextEvent.sunEventType }
          .last { it.instant.isBefore(place.instant) }

      val events = listOf(lastEvent) + futureEvents
      return SunTimeSeries(events, place)
    }

    private fun toEvents(sunTimes: SunTimes): List<Event> {
      return SunEventType.entries
        .map { Pair(it, it.instantOf(sunTimes)) }
        .filter { it.second != null }
        .map { Event(it.first, it.second!!) }
        .sorted()
    }
  }
}
