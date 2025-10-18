package org.onereed.helios.common

import android.location.Location
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes

/** Represents a latitude-longitude-altitude location.  */
data class Place(val latDeg: Double, val lonDeg: Double, val altitudeMeters: Double) {

    constructor(location: Location) : this(location.latitude, location.longitude, location.altitude)

    fun asPositionParameters(): SunPosition.Parameters {
        return SunPosition.compute().at(latDeg, lonDeg).elevation(altitudeMeters)
    }

    fun asTimesParameters(): SunTimes.Parameters {
        return SunTimes.compute().at(latDeg, lonDeg).elevation(altitudeMeters)
    }
}
