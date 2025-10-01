package org.onereed.helios.common;

/** Static utility methods for working with angular directions. */
public class DirectionUtil {

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
