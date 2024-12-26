package org.onereed.helios.common;

import android.hardware.GeomagneticField;

import java.time.Instant;

/** Static utility methods for working with angular directions. */
public class DirectionUtil {

  /**
   * Returns the magnetic declination in degrees. Positive values indicate that magnetic north is
   * east of true north. See <a href="https://en.wikipedia.org/wiki/Magnetic_declination">this
   * Wikipedia page</a>.
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

  /**
   * Normalizes the argument angle into the range [-180..180) as a float, since that's what all the
   * Android rotation methods want.
   */
  public static float zeroCenterDeg(double deg) {
    float centered = (float) deg;

    while (centered < -180.0f) {
      centered += 360.0f;
    }

    while (centered >= 180.0f) {
      centered -= 360.0f;
    }

    return centered;
  }

  private DirectionUtil() {}
}
