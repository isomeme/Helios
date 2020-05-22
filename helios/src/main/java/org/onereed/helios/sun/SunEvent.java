package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

/** Represents one sun event -- rise, noon, set, or nadir. */
@AutoValue
public abstract class SunEvent implements Comparable<SunEvent> {

  private static final Comparator<SunEvent> COMPARATOR =
      Comparator.comparing(SunEvent::getTime).thenComparing(SunEvent::getType);

  /**
   * If two events of the same type are at least this far apart, they are probably two
   * different events, not differently-calculated copies of the same one.
   */
  private static final Duration DIFFERENT_EVENT_SEPARATION = Duration.ofHours(3L);

  public enum Type {
    RISE(SunTimes::getRise),
    NOON(SunTimes::getNoon),
    SET(SunTimes::getSet),
    NADIR(SunTimes::getNadir);

    private final Function<SunTimes, Date> dateExtractor;

    Type(Function<SunTimes, Date> dateExtractor) {
      this.dateExtractor = dateExtractor;
    }

    /**
     * Returns the {@link SunEvent} corresponding to this {@link Type}, in the given {@link
     * SunTimes} instance, if it is available. Rise and set events will not be available for arctic
     * summer and winter.
     */
    Optional<SunEvent> createSunEvent(@NonNull SunTimes sunTimes) {
      return Optional.ofNullable(dateExtractor.apply(sunTimes))
          .map(Date::toInstant)
          .map(instant -> SunEvent.builder().setTime(instant).setType(this).build());
    }
  }

  @NonNull
  public abstract Instant getTime();

  @NonNull
  public abstract Type getType();

  /** True if this event is the closest one to the time of calculation. */
  public abstract boolean isClosest();

  @Override
  public int compareTo(@NonNull SunEvent o) {
    return COMPARATOR.compare(this, o);
  }

  /**
   * Our {@link SunEvent} calculations can result in two copies of the same event at slightly
   * different times. This method returns true when this event is probably a duplicate of the
   * argument.
   */
  boolean isDuplicateOf(SunEvent o) {
    return getType().equals(o.getType())
        && Duration.between(getTime(), o.getTime()).abs().compareTo(DIFFERENT_EVENT_SEPARATION) < 0;
  }

  /**
   * Returns a {@link SunEvent.Builder}. As a convenience, {@code setClosest} is set to
   * {@code false}.
   */
  static Builder builder() {
    return new AutoValue_SunEvent.Builder().setClosest(false);
  }

  abstract Builder toBuilder();

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setTime(Instant time);
    abstract Builder setType(Type type);
    abstract Builder setClosest(boolean isClosest);

    abstract SunEvent build();
  }
}
