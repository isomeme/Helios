package org.onereed.helios.sun

import org.onereed.helios.common.Place
import org.shredzone.commons.suncalc.SunTimes
import timber.log.Timber
import java.time.Duration
import java.time.Instant

data class SunInfo(
    val instant: Instant,
    val sunAzimuthInfo: SunAzimuthInfo,
    val closestEventIndex: Int,
    val sunEvents: List<SunEvent>
) {
    companion object {

        /**
         * We use this value combined with [.PRECEDING_LIMIT] to include "preceding" events up to
         * and beyond the next upcoming event, to avoid edge cases where an event happening near the time
         * we're checking falls out of both the preceding and upcoming events lists. We then remove
         * duplicates in the preceding events.
         */
        private val PRECEDING_OFFSET = Duration.ofHours(13L)

        /** See [.PRECEDING_OFFSET].  */
        private val PRECEDING_LIMIT = Duration.ofHours(14L)

        /**
         * We want to display e.g. a sunset that happens 24 hrs 1 minute from now. But we don't want to
         * confuse the display by showing a sunset that won't happen for a long time (e.g. in arctic
         * summer). We limit the search for future events to this long from now.
         */
        private val FUTURE_LIMIT = Duration.ofHours(36L)

        fun compute(place: Place, instant: Instant): SunInfo {
            Timber.d("place=$place instant=$instant")

            val parameters = place.asTimesParameters()
            val nextSunTimes = parameters.on(instant).limit(FUTURE_LIMIT).execute()
            val nextEvents = toSunEvents(nextSunTimes, place)
            val nextEvent = nextEvents.first()

            val precedingTime = nextEvent.instant.minus(PRECEDING_OFFSET)
            val precedingSunTimes = parameters.on(precedingTime).limit(PRECEDING_LIMIT).execute()
            val precedingEvents = toSunEvents(precedingSunTimes, place)
            val mostRecentEvent = getMostRecentEvent(precedingEvents, nextEvent)

            val sunAzimuthInfo = SunAzimuthInfo.from(place, instant)
            val closestEventIndex = getClosestEventIndex(instant, mostRecentEvent, nextEvent)
            val shownSunEvents = listOf(mostRecentEvent).plus(nextEvents)

            return SunInfo(instant, sunAzimuthInfo, closestEventIndex, shownSunEvents)
        }

        private fun toSunEvents(sunTimes: SunTimes, place: Place): List<SunEvent> {
            return SunEvent.Type.entries.mapNotNull { it.createSunEvent(sunTimes, place) }.sorted()
        }

        private fun getMostRecentEvent(
            precedingEvents: List<SunEvent>, nextEvent: SunEvent
        ): SunEvent {
            return precedingEvents.filter { it.type != nextEvent.type }
                .last { it.instant.isBefore(nextEvent.instant) }
        }

        private fun getClosestEventIndex(
            instant: Instant, mostRecentEvent: SunEvent, nextEvent: SunEvent
        ): Int {
            val beforeTime = mostRecentEvent.instant
            val afterTime = nextEvent.instant
            val between = Duration.between(beforeTime, afterTime)
            val halfway = beforeTime.plus(between.dividedBy(2L))
            return if (instant.isBefore(halfway)) 0 else 1
        }
    }
}
