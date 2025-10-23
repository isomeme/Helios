package org.onereed.helios.sun

import java.time.Duration
import java.time.Instant

data class SunSchedule(val events: List<Event>) {
  data class Event(
    val sunEventType: SunEventType,
    val instant: Instant,
    val isClosestEvent: Boolean,
    val weakId: Long,
  )

  companion object {

    fun compute(sunTimeSeries: SunTimeSeries): SunSchedule {
      val closestEventIndex =
        getClosestEventIndex(
          sunTimeSeries.placeTime.instant,
          sunTimeSeries.events[0].instant,
          sunTimeSeries.events[1].instant,
        )

      val events =
        sunTimeSeries.events.mapIndexed { index, event ->
          val isClosestEvent = index == closestEventIndex
          val weakId = weakIdOf(event)
          Event(event.sunEventType, event.instant, isClosestEvent, weakId)
        }

      return SunSchedule(events)
    }

    private fun getClosestEventIndex(now: Instant, t0: Instant, t1: Instant): Int =
      if (Duration.between(t0, now) < Duration.between(now, t1)) 0 else 1

    /**
     * When applied with bitwise `and` to the sun event epoch second, yields a time bucket within
     * which two events with different times and the same [SunEventType] might actually be the same
     * event. The bucket size is 2^14 = 16,384 seconds, or roughly 4.6 hours.
     */
    private const val EVENT_TIME_BUCKET_MASK = 0x3FFFL.inv()

    /**
     * We calculate the weak ID for the event by separating time into ~4.6 hour buckets and adding
     * the type ordinal to distinguish events of different types within the bucket. This is used in
     * the UI to identify when a newly delivered event is probably the same as one from the previous
     * update.
     */
    private fun weakIdOf(event: SunTimeSeries.Event) =
      (event.instant.epochSecond and EVENT_TIME_BUCKET_MASK) + event.sunEventType.ordinal
  }
}
