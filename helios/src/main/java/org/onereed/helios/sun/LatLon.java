package org.onereed.helios.sun;

import android.location.Location;

import com.google.auto.value.AutoValue;

/** Represents a latitude-longitude coordinate pair. */
@AutoValue
public abstract class LatLon {

  abstract double getLat();
  abstract double getLon();

  double[] asArray() {
    return new double[]{getLat(), getLon()};
  }

  public static LatLon from(Location location) {
    return new AutoValue_LatLon(location.getLatitude(), location.getLongitude());
  }

  /** Intended for testing. */
  static LatLon of(double lat, double lon) {
    return new AutoValue_LatLon(lat, lon);
  }
}
