package org.onereed.helios.sun

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaDuration
import kotlin.time.toJavaInstant
import org.onereed.helios.common.PlaceTime
import org.shredzone.commons.suncalc.SunTimes

@OptIn(ExperimentalTime::class)
class SunTimeSeries(val placeTime: PlaceTime) {
  data class Event(val sunEventType: SunEventType, val instant: Instant) : Comparable<Event> {
    override fun compareTo(other: Event) =
      compareValuesBy(this, other, { it.instant }, { it.sunEventType })
  }

  val events: List<Event>

  init {
    val futureSunTimes = placeTime.computeSunTimes(FUTURE_LIMIT)
    val futureEvents = toEvents(futureSunTimes)
    val nextEvent = futureEvents.first()

    val earlierPlaceTime = placeTime.copy(instant = placeTime.instant - PRECEDING_OFFSET)
    val pastSunTimes = earlierPlaceTime.computeSunTimes(PRECEDING_LIMIT)
    val pastEvents = toEvents(pastSunTimes)
    val lastEvent =
      pastEvents
        .filter { it.sunEventType != nextEvent.sunEventType }
        .last { it.instant < placeTime.instant }

    this.events = listOf(lastEvent) + futureEvents
  }

  companion object {

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

    private fun PlaceTime.computeSunTimes(limit: Duration): SunTimes =
      SunTimes.compute()
        .at(lat, lon)
        .elevation(alt)
        .on(instant.toJavaInstant())
        .limit(limit.toJavaDuration())
        .execute()

    private fun toEvents(sunTimes: SunTimes): List<Event> {
      return SunEventType.entries
        .map { Pair(it, it.instantOf(sunTimes)) }
        .filter { it.second != null }
        .map { Event(it.first, it.second!!) }
        .sorted()
    }
  }
}
