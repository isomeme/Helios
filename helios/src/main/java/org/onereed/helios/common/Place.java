package org.onereed.helios.common;

import android.location.Location;

import com.google.auto.value.AutoValue;

/** Represents a latitude-longitude-altitude location. */
@AutoValue
public abstract class Place {

  public abstract double getLatDeg();

  public abstract double getLonDeg();

  public abstract double getAltitudeMeters();

  public static Place from(Location location) {
    return new AutoValue_Place(
        location.getLatitude(), location.getLongitude(), location.getAltitude());
  }

  /** Intended for testing. */
  public static Place of(double latDeg, double lonDeg, double altitudeMeters) {
    return new AutoValue_Place(latDeg, lonDeg, altitudeMeters);
  }
}
