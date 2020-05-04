package org.onereed.helios.sun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.shredzone.commons.suncalc.SunTimes;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Function;

/**
 * Represents one sun event -- rise, noon, set, or nadir.
 */
@AutoValue
public abstract class SunEvent implements Comparable<SunEvent> {

  private static final Comparator<SunEvent> COMPARATOR =
      Comparator.comparing(SunEvent::getTime).thenComparing(SunEvent::getType);

  public enum Type {
    RISE(SunTimes::getRise),
    NOON(SunTimes::getNoon),
    SET(SunTimes::getSet),
    NADIR(SunTimes::getNadir);

    private final Function<SunTimes, Date> dateExtractor;

    Type(Function<SunTimes, Date> dateExtractor) {
      this.dateExtractor = dateExtractor;
    }

    @Nullable
    Date getDate(SunTimes sunTimes) {
      return dateExtractor.apply(sunTimes);
    }
  }

  @NonNull
  public abstract Instant getTime();

  @NonNull
  public abstract Type getType();

  static SunEvent create(Instant time, Type type) {
    return new AutoValue_SunEvent(time, type);
  }

  @Override
  public int compareTo(@NonNull SunEvent o) {
    return COMPARATOR.compare(this, o);
  }
}
