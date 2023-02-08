package org.onereed.helios.common;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;

import java.time.Duration;

/** Static utility constants and methods for working with locations. */
public final class LocationUtil {

  private static final long FASTEST_INTERVAL_MILLIS = Duration.ofSeconds(30L).toMillis();
  private static final long INTERVAL_MILLIS = Duration.ofMinutes(1L).toMillis();

  /** Standard request for repeated location updates. */
  public static final LocationRequest REPEATED_LOCATION_REQUEST =
      new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, INTERVAL_MILLIS)
          .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MILLIS)
          .build();
}
