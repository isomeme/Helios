package org.onereed.helios.datasource

import android.location.Location
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Represents a point in latitude, longitude, altitude, and time. */
@OptIn(ExperimentalTime::class)
data class PlaceTime(val place: Place, val time: Instant, val isValid: Boolean = true) {
  data class Place(val lat: Double, val lon: Double, val alt: Double = 0.0) {
    constructor(location: Location) : this(location.latitude, location.longitude, location.altitude)
  }

  companion object {
    val INVALID = PlaceTime(Place(0.0, 0.0), Instant.DISTANT_PAST, isValid = false)
  }
}
