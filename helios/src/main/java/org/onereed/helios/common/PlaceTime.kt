package org.onereed.helios.common

import android.location.Location
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Represents a point in latitude, longitude, altitude, and time. */
@OptIn(ExperimentalTime::class)
data class PlaceTime(val lat: Double, val lon: Double, val alt: Double, val instant: Instant) {

  constructor(
    location: Location,
    instant: Instant,
  ) : this(location.latitude, location.longitude, location.altitude, instant)
}
