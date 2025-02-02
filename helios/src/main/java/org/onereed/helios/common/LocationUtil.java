package org.onereed.helios.common;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import java.time.Duration;

/** Static utility constants and methods for working with locations. */
public final class LocationUtil {

  private static final Duration UPDATE_INTERVAL = Duration.ofMinutes(1L);
  private static final Duration FASTEST_UPDATE_INTERVAL = Duration.ofSeconds(30L);

  /** Standard request for repeated location updates. */
  public static final LocationRequest REPEATED_LOCATION_REQUEST =
      new LocationRequest.Builder(
              Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL.toMillis())
          .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL.toMillis())
          .build();
}
