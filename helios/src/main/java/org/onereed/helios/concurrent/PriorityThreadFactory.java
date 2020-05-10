package org.onereed.helios.concurrent;

import android.os.Process;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A factory for {@link Thread} instances with a particular Android priority.
 */
class PriorityThreadFactory implements ThreadFactory {

  private final AtomicLong threadNumberSource = new AtomicLong();

  private final String name;

  private final int priority;

  PriorityThreadFactory(@NonNull String name, int priority) {
    this.name = name;
    this.priority = priority;
  }

  @Override
  public Thread newThread(@NonNull Runnable runnable) {
    return new Thread(() -> wrapRunnable(runnable), generateThreadName());
  }

  private void wrapRunnable(@NonNull Runnable runnable) {
    Process.setThreadPriority(priority);
    runnable.run();
  }

  private String generateThreadName() {
    return String.format(Locale.ENGLISH, "%s-%d", name, threadNumberSource.incrementAndGet());
  }

  @NonNull
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("priority", priority).toString();
  }
}
