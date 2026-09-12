package org.onereed.helios.datasource

import android.location.Location
import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Represents a point in latitude, longitude, altitude, and time. */
@OptIn(ExperimentalTime::class)
@Immutable
data class PlaceTime(val place: Place, val time: Instant) {
  @Immutable
  data class Place(val lat: Double, val lon: Double, val alt: Double = 0.0) {
    constructor(location: Location) : this(location.latitude, location.longitude, location.altitude)
  }
}
