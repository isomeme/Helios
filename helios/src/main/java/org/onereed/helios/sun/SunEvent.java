package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Comparator;

/**
 * Represents one sun event -- rise, noon, set, or nadir.
 */
@AutoValue
public abstract class SunEvent implements Comparable<SunEvent> {

  private static final Comparator<SunEvent> COMPARATOR =
      Comparator.comparing(SunEvent::getTime).thenComparing(SunEvent::getType);

  public enum Type {
    RISE,
    NOON,
    SET,
    NADIR
  }

  public abstract Instant getTime();

  public abstract Type getType();

  static SunEvent create(Instant time, Type type) {
    return new AutoValue_SunEvent(time, type);
  }

  @Override
  public int compareTo(@NonNull SunEvent o) {
    return COMPARATOR.compare(this, o);
  }
}
