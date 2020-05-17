package org.onereed.helios.common;

import com.google.android.gms.location.LocationRequest;

import java.time.Duration;

/**
 * Static utility constants and methods for working with locations.
 */
public final class LocationUtil {

  private static final long FASTEST_INTERVAL_MILLIS = Duration.ofSeconds(30L).toMillis();
  private static final long INTERVAL_MILLIS = Duration.ofMinutes(2L).toMillis();

  /**
   * Standard request for repeated location updates.
   */
  public static final LocationRequest REPEATED_LOCATION_REQUEST =
      new LocationRequest()
          .setFastestInterval(FASTEST_INTERVAL_MILLIS)
          .setInterval(INTERVAL_MILLIS)
          .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
}
