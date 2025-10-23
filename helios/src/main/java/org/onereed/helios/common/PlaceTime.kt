package org.onereed.helios.common

import android.location.Location
import java.time.Duration
import java.time.Instant
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes

/** Represents a point in latitude, longitude, altitude, and time. */
data class PlaceTime(
  val latDeg: Double,
  val lonDeg: Double,
  val altMeters: Double,
  val instant: Instant,
) {

  constructor(
    location: Location,
    instant: Instant,
  ) : this(location.latitude, location.longitude, location.altitude, instant)

  fun atInstant(instant: Instant): PlaceTime = copy(instant = instant)

  fun plusDuration(duration: Duration): PlaceTime = copy(instant = instant.plus(duration))

  fun computeSunAzimuth(): Double =
    SunPosition.compute().at(latDeg, lonDeg).elevation(altMeters).on(instant).execute().azimuth

  fun computeSunTimes(limit: Duration): SunTimes =
    SunTimes.compute().at(latDeg, lonDeg).elevation(altMeters).on(instant).limit(limit).execute()
}
