package org.onereed.helios.sun

import java.time.Instant
import java.time.ZonedDateTime
import java.util.function.Function
import org.onereed.helios.common.Place
import org.shredzone.commons.suncalc.SunTimes

/** Represents one sun event -- rise, noon, set, or nadir. */
data class SunEvent(
  val type: Type,
  val instant: Instant,
  val azimuthDeg: Double,
  val weakId: Long = createWeakId(instant, type),
) : Comparable<SunEvent> {

  enum class Type(private val timeExtractor: Function<SunTimes, ZonedDateTime?>) {
    RISE(SunTimes::getRise),
    NOON(SunTimes::getNoon),
    SET(SunTimes::getSet),
    NADIR(SunTimes::getNadir);

    /**
     * Returns the [SunEvent] corresponding to this [Type], in the given [SunTimes] instance, if it
     * is available. Rise and set events will not be available for arctic summer and winter.
     */
    fun createSunEvent(sunTimes: SunTimes, place: Place): SunEvent? {
      return timeExtractor.apply(sunTimes)?.toInstant()?.let { instant ->
        SunEvent(this, instant, place.asPositionParameters().on(instant).execute().azimuth)
      }
    }
  }

  override fun compareTo(other: SunEvent) =
    compareValuesBy(this, other, { it.instant }, { it.type })

  companion object {

    /**
     * When applied with bitwise `and` to the sun event epoch second, yields a time bucket within
     * which two events with different times and the same [Type] might actually be the same event.
     * The bucket size is 2^14 = 16,384 seconds, or roughly 4.6 hours.
     */
    private const val EVENT_TIME_BUCKET_MASK = 0x3FFFL.inv()

    /**
     * We create a weak ID for the event by separating time into ~4.6 hour buckets and adding the
     * type ordinal to distinguish events of different types within the bucket. This is used in the
     * UI to identify when a newly delivered event is probably the same as one from the previous
     * update.
     */
    fun createWeakId(instant: Instant, type: Type) =
      (instant.epochSecond and EVENT_TIME_BUCKET_MASK) + type.ordinal
  }
}
