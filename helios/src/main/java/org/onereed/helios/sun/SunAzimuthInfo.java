package org.onereed.helios.sun;

import androidx.annotation.VisibleForTesting;

import com.google.auto.value.AutoValue;

import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.Place;

import java.time.Duration;
import java.time.Instant;

/** Sun azimuth and azimuth direction (clockwise or counterclockwise). */
@AutoValue
public abstract class SunAzimuthInfo {

  private static final Duration DELTA_TIME = Duration.ofMinutes(1L);

  public abstract float getAzimuthDeg();

  public abstract boolean isClockwise();

  static SunAzimuthInfo from(Place where, Instant when) {
    double azimuthNow = SunCalcUtil.getSunAzimuthDeg(where, when);
    double azimuthSoon = SunCalcUtil.getSunAzimuthDeg(where, when.plus(DELTA_TIME));
    double deltaAzimuth = DirectionUtil.zeroCenterDeg(azimuthSoon - azimuthNow);

    return create((float) azimuthNow, /* clockwise= */ deltaAzimuth >= 0.0);
  }

  @VisibleForTesting
  static SunAzimuthInfo create(float azimuthDeg, boolean isClockwise) {
    return new AutoValue_SunAzimuthInfo(azimuthDeg, isClockwise);
  }
}
