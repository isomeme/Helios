package org.onereed.helios.concurrent;

import android.os.Process;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Static factories for Android-specific {@link Executor} instances.
 */
public class AppExecutors {

  private static final String TAG = LogUtil.makeTag(AppExecutors.class);

  /**
   * Number of cores to decide the number of threads
   */
  private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

  private static final Duration KEEP_ALIVE_TIME = Duration.ofMinutes(1L);

  private static final ThreadFactory BACKGROUND_THREAD_FACTORY =
      new PriorityThreadFactory("appBackground", Process.THREAD_PRIORITY_BACKGROUND);

  public static Executor createBackgroundExecutor() {
    int poolSize = NUMBER_OF_CORES * 2;
    AppLogger.debug(TAG, "Creating background executor, poolSize=%d", poolSize);

    return new ThreadPoolExecutor(
        poolSize,
        poolSize,
        KEEP_ALIVE_TIME.toMillis(),
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        BACKGROUND_THREAD_FACTORY);
  }

  private AppExecutors() {
  }
}
