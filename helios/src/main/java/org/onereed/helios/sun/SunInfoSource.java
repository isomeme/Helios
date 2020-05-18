package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.onereed.helios.concurrent.BackgroundThreadPoolExecutor;

import java.time.Instant;

/** Provides a static method for asynchronous {@link SunInfo} requests. */
public class SunInfoSource {

  private static final BackgroundThreadPoolExecutor sunInfoExecutor =
      BackgroundThreadPoolExecutor.create("sunInfo");

  public static Task<SunInfo> request(double lat, double lon, @NonNull Instant when) {
    return Tasks.call(sunInfoExecutor, () -> SunInfoUtil.getSunInfo(lat, lon, when));
  }

  private SunInfoSource() {}
}
