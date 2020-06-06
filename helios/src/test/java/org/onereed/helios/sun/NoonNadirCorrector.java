package org.onereed.helios.sun;

import org.shredzone.commons.suncalc.SunPosition;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Provides a static method that corrects SunCalc noon and nadir times to within +- 0.25 degree (one
 * minute of solar motion across the sky) of 0 or 180 degrees azimuth.
 */
class NoonNadirCorrector {

  private static final double AZIMUTH_EPSILON = 0.25;
  private static final long INITIAL_DELTA_TIME_MILLIS = Duration.ofMinutes(5L).toMillis();
  private static final int MAX_STEPS = 5;

  static Date correct(Date eventTime, double[] coords) {
    SunPosition eventSunPosition = SunPosition.compute().on(eventTime).at(coords).execute();
    double eventAzimuth = eventSunPosition.getAzimuth();

    // Select an azimuth offset that lets us aim for (azimuth + correction) % 360 near 180. We use
    // 180 rather than 0 because in Java the % operator only works the way we want on positive
    // values.

    double azimuthOffset = Math.abs(eventAzimuth - 180.0) < 90.0 ? 0.0 : 180.0;

    double lastOffsetAzimuth = (eventAzimuth + azimuthOffset) % 360.0;
    Instant lastTime = eventTime.toInstant();
    long deltaTimeMillis = INITIAL_DELTA_TIME_MILLIS;
    int stepCount;

    for (stepCount = 0; stepCount <= MAX_STEPS; ++stepCount) {
      System.out.printf(
          "lastOffsetAzimuth=%.3f lastTime=%s stepCount=%d\n",
          lastOffsetAzimuth, lastTime, stepCount);

      double lastAzimuthError = 180.0 - lastOffsetAzimuth;

      if (Math.abs(lastAzimuthError) < AZIMUTH_EPSILON) {
        break;
      }

      Instant nextTime = lastTime.plusMillis(deltaTimeMillis);
      SunPosition nextPosition =
          SunPosition.compute().on(Date.from(nextTime)).at(coords).execute();
      double nextOffsetAzimuth = (nextPosition.getAzimuth() + azimuthOffset) % 360.0;
      double nextAzimuthError = 180.0 - nextOffsetAzimuth;
      double deltaAzimuth = nextOffsetAzimuth - lastOffsetAzimuth;

      lastOffsetAzimuth = nextOffsetAzimuth;
      lastTime = nextTime;
      deltaTimeMillis = (long) (deltaTimeMillis * (nextAzimuthError / deltaAzimuth));
    }

    if (stepCount > MAX_STEPS) {
      System.out.print("Max step count exceeded.\n");

      // This probably means we've gone unstable, so return the original time.
      return eventTime;
    }

    return Date.from(lastTime);
  }

  private NoonNadirCorrector() {}
}
