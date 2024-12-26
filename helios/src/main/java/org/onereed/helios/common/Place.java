package org.onereed.helios.common;

import android.location.Location;
import com.google.auto.value.AutoValue;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

/** Represents a latitude-longitude-altitude location. */
@AutoValue
public abstract class Place {

  public abstract double getLatDeg();

  public abstract double getLonDeg();

  public abstract double getAltitudeMeters();

  public SunPosition.Parameters asPositionParameters() {
    return SunPosition.compute().at(getLatDeg(), getLonDeg()).elevation(getAltitudeMeters());
  }

  public SunTimes.Parameters asTimesParameters() {
    return SunTimes.compute().at(getLatDeg(), getLonDeg()).elevation(getAltitudeMeters());
  }

  public static Place from(Location location) {
    return new AutoValue_Place(
        location.getLatitude(), location.getLongitude(), location.getAltitude());
  }

  /** Intended for testing. */
  public static Place of(double latDeg, double lonDeg, double altitudeMeters) {
    return new AutoValue_Place(latDeg, lonDeg, altitudeMeters);
  }
}
