package org.onereed.helios.sun

import java.time.ZonedDateTime
import java.util.function.Function
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import org.shredzone.commons.suncalc.SunTimes

/** The four sun event types -- rise, noon, set, and nadir. */
enum class SunEventType(private val timeExtractor: Function<SunTimes, ZonedDateTime?>) {
  RISE(SunTimes::getRise),
  NOON(SunTimes::getNoon),
  SET(SunTimes::getSet),
  NADIR(SunTimes::getNadir);

  /** Returns the instant of the [SunEventType] in [sunTimes] if it is present. */
  @OptIn(ExperimentalTime::class)
  fun instantOf(sunTimes: SunTimes): Instant? =
    timeExtractor.apply(sunTimes)?.toInstant()?.toKotlinInstant()
}
