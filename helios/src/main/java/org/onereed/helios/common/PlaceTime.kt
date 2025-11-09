package org.onereed.helios.common

import android.location.Location
import java.time.Duration
import java.time.Instant

/** Represents a point in latitude, longitude, altitude, and time. */
data class PlaceTime(val lat: Double, val lon: Double, val alt: Double, val instant: Instant) {

  constructor(
    location: Location,
    instant: Instant,
  ) : this(location.latitude, location.longitude, location.altitude, instant)

  fun atInstant(instant: Instant): PlaceTime = copy(instant = instant)

  fun plusDuration(duration: Duration): PlaceTime = copy(instant = instant.plus(duration))

  fun minusDuration(duration: Duration): PlaceTime = copy(instant = instant.minus(duration))

  companion object {

    val NONE = PlaceTime(0.0, 0.0, 0.0, Instant.EPOCH)
  }
}
