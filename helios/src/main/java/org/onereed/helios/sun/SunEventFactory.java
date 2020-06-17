package org.onereed.helios.sun;

import com.google.common.collect.ImmutableSet;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.Place;
import org.onereed.helios.logger.AppLogger;

import java.time.Duration;
import java.time.Instant;

/**
 * Provides a static method that creates {@link SunEvent} instances. Noon and nadir events are
 * iteratively corrected to be close to an azimuth of 0 or 180 degrees (SunCalc sometimes gets
 * this wrong).
 */
class SunEventFactory {

  private static final String TAG = LogUtil.makeTag(SunEventFactory.class);

  private static final ImmutableSet<SunEvent.Type> CORRECTED_TYPES =
      ImmutableSet.of(SunEvent.Type.NOON, SunEvent.Type.NADIR);

  private static final double AZIMUTH_EPSILON = 0.25;
  private static final long INITIAL_DELTA_TIME_MILLIS = Duration.ofMinutes(5L).toMillis();
  private static final int MAX_STEPS = 5;

  static SunEvent create(SunEvent.Type type, Place where, Instant when) {
    double eventAzimuth = SunCalcUtil.getSunAzimuthDeg(where, when);

    if (!CORRECTED_TYPES.contains(type)) {
      return SunEvent.create(type, when, eventAzimuth);
    }

    // Select an azimuth offset that lets us aim for (azimuth + correction) % 360 near 180. We use
    // 180 rather than 0 because in Java the % operator only works the way we want on positive
    // values.

    double azimuthOffset = Math.abs(eventAzimuth - 180.0) < 90.0 ? 0.0 : 180.0;

    double lastAzimuth = eventAzimuth;
    double lastOffsetAzimuth = (eventAzimuth + azimuthOffset) % 360.0;
    Instant lastTime = when;
    long deltaTimeMillis = INITIAL_DELTA_TIME_MILLIS;
    int stepCount;

    for (stepCount = 0; stepCount <= MAX_STEPS; ++stepCount) {
      AppLogger.debug(
          TAG,
          "lastOffsetAzimuth=%.3f lastTime=%s stepCount=%d",
          lastOffsetAzimuth, lastTime, stepCount);

      double lastAzimuthError = 180.0 - lastOffsetAzimuth;

      if (Math.abs(lastAzimuthError) < AZIMUTH_EPSILON) {
        break;
      }

      Instant nextTime = lastTime.plusMillis(deltaTimeMillis);
      double nextAzimuth = SunCalcUtil.getSunAzimuthDeg(where, nextTime);
      double nextOffsetAzimuth = (nextAzimuth + azimuthOffset) % 360.0;
      double nextAzimuthError = 180.0 - nextOffsetAzimuth;
      double deltaAzimuth = nextOffsetAzimuth - lastOffsetAzimuth;

      lastAzimuth = nextAzimuth;
      lastOffsetAzimuth = nextOffsetAzimuth;
      lastTime = nextTime;
      deltaTimeMillis = (long) (deltaTimeMillis * (nextAzimuthError / deltaAzimuth));
    }

    if (stepCount > MAX_STEPS) {
      AppLogger.warning(TAG, "Max step count exceeded.");

      // This probably means we've gone unstable, so return the original SunCalc data
      lastAzimuth = eventAzimuth;
      lastTime = when;
    }

    return SunEvent.create(type, lastTime, lastAzimuth);
  }

  private SunEventFactory() {}
}
