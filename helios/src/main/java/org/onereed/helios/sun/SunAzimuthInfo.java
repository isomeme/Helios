package org.onereed.helios.sun;

import androidx.annotation.VisibleForTesting;
import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.time.Instant;
import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.Place;
import org.shredzone.commons.suncalc.SunPosition;

/** Sun azimuth and azimuth direction (clockwise or counterclockwise). */
@AutoValue
public abstract class SunAzimuthInfo {

  private static final Duration DELTA_TIME = Duration.ofMinutes(1L);

  public abstract float getAzimuthDeg();

  public abstract boolean isClockwise();

  static SunAzimuthInfo from(Place where, Instant when) {
    SunPosition.Parameters parameters = where.asPositionParameters();
    double azimuthNow = parameters.on(when).execute().getAzimuth();
    Instant when1 = when.plus(DELTA_TIME);
    double azimuthSoon = parameters.on(when1).execute().getAzimuth();
    float deltaAzimuth = DirectionUtil.zeroCenterDeg(azimuthSoon - azimuthNow);

    return create((float) azimuthNow, /* clockwise= */ deltaAzimuth >= 0.0);
  }

  @VisibleForTesting
  static SunAzimuthInfo create(float azimuthDeg, boolean isClockwise) {
    return new AutoValue_SunAzimuthInfo(azimuthDeg, isClockwise);
  }
}
