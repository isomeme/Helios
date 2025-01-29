package org.onereed.helios.sun;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.time.Instant;
import org.onereed.helios.common.Place;
import org.onereed.helios.concurrent.BackgroundThreadPoolExecutor;

/** Provides a static method for asynchronous {@link SunInfo} requests. */
public class SunInfoSource {

  private static final BackgroundThreadPoolExecutor sunInfoExecutor =
      BackgroundThreadPoolExecutor.create("sunInfo");

  public static Task<SunInfo> request(@NonNull Place where, @NonNull Instant when) {
    var taskCompletionSource = new TaskCompletionSource<SunInfo>();
    sunInfoExecutor.execute(
        () -> taskCompletionSource.setResult(SunInfoUtil.getSunInfo(where, when)));
    return taskCompletionSource.getTask();
  }

  private SunInfoSource() {}
}
