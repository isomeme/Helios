package org.onereed.helios.sun;

import org.onereed.helios.common.Place;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Instant;

/** Static utility methods for working with SunCalc. */
class SunCalcUtil {

  static double getSunAzimuthDeg(Place where, Instant when) {
    return SunPosition.compute()
        .at(where.getLatDeg(), where.getLonDeg())
        .height(where.getAltitudeMeters())
        .on(when)
        .execute()
        .getAzimuth();
  }

  static SunTimes getSunTimes(Place where, Instant when) {
    return SunTimes.compute()
        .at(where.getLatDeg(), where.getLonDeg())
        .height(where.getAltitudeMeters())
        .on(when)
        .execute();
  }

  private SunCalcUtil() {}
}
