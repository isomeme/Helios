package org.onereed.helios.common;

import android.hardware.GeomagneticField;

import java.time.Instant;

/** Static utility methods for working with angular directions. */
public class DirectionUtil {

  /**
   * Returns the magnetic declination in degrees. Positive values indicate that magnetic north is
   * east of true north. See https://en.wikipedia.org/wiki/Magnetic_declination
   */
  // TODO: Using GeomagneticField forces us to turn on unitTests.returnDefaultValues. More reason
  // to figure out Dagger.
  public static double getMagneticDeclinationDeg(Place where, Instant when) {
    return new GeomagneticField(
            (float) where.getLatDeg(),
            (float) where.getLonDeg(),
            (float) where.getAltitudeMeters(),
            when.toEpochMilli())
        .getDeclination();
  }

  /** Normalizes the argument angle into the range [-180..180). */
  public static double zeroCenterDeg(double deg) {
    while (deg < -180.0) {
      deg += 360.0;
    }

    while (deg >= 180.0) {
      deg -= 360.0;
    }

    return deg;
  }

  private DirectionUtil() {}
}
