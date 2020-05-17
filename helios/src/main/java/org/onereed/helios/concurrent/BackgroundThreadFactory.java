package org.onereed.helios.concurrent;

import android.os.Process;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link ThreadFactory} which returns {@link Thread} instances set to run at background priority.
 */
class BackgroundThreadFactory implements ThreadFactory {

  private final AtomicLong threadNumberSource = new AtomicLong();

  private final String nameBase;

  BackgroundThreadFactory(String nameBase) {
    this.nameBase = nameBase;
  }

  @Override
  public Thread newThread(@NonNull Runnable runnable) {
    Runnable wrapperRunnable =
        () -> {
          Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
          runnable.run();
        };
    String name =
        String.format(Locale.ENGLISH, "%s-%d", nameBase, threadNumberSource.incrementAndGet());
    return new Thread(null, wrapperRunnable, name);
  }
}
