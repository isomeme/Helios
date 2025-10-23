package org.onereed.helios.sun

import java.time.Instant
import org.onereed.helios.common.PlaceTime
import org.shredzone.commons.suncalc.SunTimes

/** Represents one sun event -- rise, noon, set, or nadir. */
data class SunEvent(
  val sunEventType: SunEventType,
  val instant: Instant,
  val azimuthDeg: Double,
  val weakId: Long = createWeakId(instant, sunEventType),
) : Comparable<SunEvent> {

  override fun compareTo(other: SunEvent) =
    compareValuesBy(this, other, { it.instant }, { it.sunEventType })

  companion object {

    /**
     * Returns the [SunEvent] corresponding to [sunEventType] in [sunTimes], if it is available.
     * Rise and set events will not be available for arctic summer and winter.
     */
    fun from(sunEventType: SunEventType, sunTimes: SunTimes, placeTime: PlaceTime): SunEvent? {
      return sunEventType.instantOf(sunTimes)?.let { instant ->
        SunEvent(sunEventType, instant, placeTime.atInstant(instant).computeSunAzimuth())
      }
    }

    /**
     * When applied with bitwise `and` to the sun event epoch second, yields a time bucket within
     * which two events with different times and the same [SunEventType] might actually be the same
     * event. The bucket size is 2^14 = 16,384 seconds, or roughly 4.6 hours.
     */
    private const val EVENT_TIME_BUCKET_MASK = 0x3FFFL.inv()

    /**
     * We create a weak ID for the event by separating time into ~4.6 hour buckets and adding the
     * type ordinal to distinguish events of different types within the bucket. This is used in the
     * UI to identify when a newly delivered event is probably the same as one from the previous
     * update.
     */
    private fun createWeakId(instant: Instant, sunEventType: SunEventType) =
      (instant.epochSecond and EVENT_TIME_BUCKET_MASK) + sunEventType.ordinal
  }
}
