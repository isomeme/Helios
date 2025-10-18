package org.onereed.helios.sun

import org.onereed.helios.common.DirectionUtil.arc
import org.onereed.helios.common.Place
import org.shredzone.commons.suncalc.SunTimes
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.util.function.Function
import kotlin.math.abs

/** Represents one sun event -- rise, noon, set, or nadir.  */
data class SunEvent(
    val type: Type, val time: Instant, val azimuthDeg: Double
) : Comparable<SunEvent> {

    enum class Type(private val timeExtractor: Function<SunTimes, ZonedDateTime?>) {
        RISE(SunTimes::getRise), NOON(SunTimes::getNoon), SET(SunTimes::getSet), NADIR(SunTimes::getNadir);

        /**
         * Returns the [SunEvent] corresponding to this [Type], in the given [SunTimes] instance, if
         * it is available. Rise and set events will not be available for arctic summer and winter.
         */
        fun createSunEvent(sunTimes: SunTimes, place: Place): SunEvent? {
            return timeExtractor.apply(sunTimes)?.toInstant()
                ?.let { SunEvent(this, it, place.asPositionParameters().on(it).execute().azimuth) }
        }
    }

    val weakId: Long
        get() {
            val timeBucket = time.epochSecond / EVENT_TIME_BUCKET_SIZE_SEC
            val ordinalOffset = TYPE_ORDINAL_SCALE * type.ordinal
            return timeBucket + ordinalOffset
        }


    override fun compareTo(other: SunEvent) = compareValuesBy(this, other, { it.time }, { it.type })

    fun isNear(other: SunEvent): Boolean {
        return abs(arc(this.azimuthDeg, other.azimuthDeg)) < 20.0
    }

    companion object {

        /**
         * Epoch seconds divided by this value yields a time bucket within which two events with
         * different times and the same [Type] might actually be the same event.
         */
        private val EVENT_TIME_BUCKET_SIZE_SEC = Duration.ofHours(4L).seconds

        /**
         * The ordinal of the this event's [Type] is multiplied by this value before being added to
         * the time bucket to yield a weak event ID. This value must be >> than the largest expected
         * time bucket value.
         */
        private const val TYPE_ORDINAL_SCALE = 10_000_000L
    }
}