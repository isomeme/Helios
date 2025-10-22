package org.onereed.helios.common

import android.location.Location
import java.time.Instant
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes

/** Represents a latitude-longitude-altitude location. */
data class Place(
  val latDeg: Double,
  val lonDeg: Double,
  val altitudeMeters: Double,
  val instant: Instant = Instant.now(),
) {

  constructor(location: Location) : this(location.latitude, location.longitude, location.altitude)

  fun asPositionParameters(): SunPosition.Parameters {
    return SunPosition.compute().at(latDeg, lonDeg).elevation(altitudeMeters)
  }

  fun asTimesParameters(): SunTimes.Parameters {
    return SunTimes.compute().at(latDeg, lonDeg).elevation(altitudeMeters)
  }
}
