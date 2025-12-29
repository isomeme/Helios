package org.onereed.helios.sun

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Immutable
class SunSchedule(sunTimeSeries: SunTimeSeries) {
  data class Event(
    val sunEventType: SunEventType,
    val time: Instant,
    val isClosestEvent: Boolean,
    val weakId: Long,
  )

  val events: List<Event>

  init {
    if (sunTimeSeries.events.isEmpty()) {
      this.events = emptyList()
    } else {
      val closestEventIndex =
        getClosestEventIndex(
          sunTimeSeries.placeTime.time,
          sunTimeSeries.events[0].time,
          sunTimeSeries.events[1].time,
        )

      this.events =
        sunTimeSeries.events.mapIndexed { index, event ->
          val isClosestEvent = index == closestEventIndex
          val weakId = weakIdOf(event)
          Event(event.sunEventType, event.time, isClosestEvent, weakId)
        }
    }
  }

  companion object {

    private fun getClosestEventIndex(now: Instant, t0: Instant, t1: Instant): Int =
      if ((now - t0) < (t1 - now)) 0 else 1

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
      (event.time.epochSeconds and EVENT_TIME_BUCKET_MASK) + event.sunEventType.ordinal
  }
}
