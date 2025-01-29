package org.onereed.helios.concurrent;

import static com.google.common.base.MoreObjects.toStringHelper;

import androidx.annotation.NonNull;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** A {@link ThreadPoolExecutor} which runs tasks at Android {@code BACKGROUND} priority. */
public class BackgroundThreadPoolExecutor extends ThreadPoolExecutor {

  private static final int POOL_SIZE = 4;
  private static final long KEEP_ALIVE_SEC = 60L;

  private final String name;

  public static BackgroundThreadPoolExecutor create(@NonNull String name) {
    return new BackgroundThreadPoolExecutor(name);
  }

  private BackgroundThreadPoolExecutor(@NonNull String name) {
    super(
        POOL_SIZE,
        POOL_SIZE,
        KEEP_ALIVE_SEC,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new BackgroundThreadFactory(name));

    this.name = name;
  }

  @NonNull
  @Override
  public String toString() {
    return toStringHelper(this)
        .add("name", name)
        .add("super", super.toString())
        .toString();
  }
}
