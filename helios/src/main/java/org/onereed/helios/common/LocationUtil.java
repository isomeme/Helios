package org.onereed.helios.common;

import com.google.android.gms.location.LocationRequest;

import java.time.Duration;

/** Static utility methods for working with locations. */
public final class LocationUtil {

  private static final long BRIEF_INTERVAL_MILLIS = 500;
  private static final long ONE_SHOT_DURATION_MILLIS = Duration.ofMinutes(5L).toMillis();

  /**
   * Creates a one-shot location request. These need to be created fresh at each usage because
   * (unbelievably) the expiration duration is relative to request *creation* time, not when the
   * request is sent. Go figure.
   */
  public static LocationRequest createOneShotLocationRequest() {
    return new LocationRequest()
        .setFastestInterval(BRIEF_INTERVAL_MILLIS)
        .setInterval(BRIEF_INTERVAL_MILLIS)
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setNumUpdates(1)
        .setExpirationDuration(ONE_SHOT_DURATION_MILLIS);
  }
}
