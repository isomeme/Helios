package org.onereed.helios.sun;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.onereed.helios.concurrent.BackgroundThreadPoolExecutor;
import org.onereed.helios.logger.AppLogger;

import java.time.Instant;

/**
 * Provides a static method for asynchronous {@link SunInfo} requests.
 */
public class SunInfoSource {

  private static final BackgroundThreadPoolExecutor sunInfoExecutor =
      BackgroundThreadPoolExecutor.create("sunInfo");

  public static Task<SunInfo> request(double lat, double lon, Instant when) {
    AppLogger.debug("helios_sis", "Running on executor=%s", sunInfoExecutor);
    return Tasks.call(sunInfoExecutor, () -> SunInfoUtil.getSunInfo(lat, lon, when));
  }

  private SunInfoSource() {
  }
}
