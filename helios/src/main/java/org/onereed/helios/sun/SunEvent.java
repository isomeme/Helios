package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.onereed.helios.R;
import org.shredzone.commons.suncalc.SunTimes;

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

  public enum Type {
    RISE(SunTimes::getRise, R.color.bg_rise),
    NOON(SunTimes::getNoon, R.color.bg_noon),
    SET(SunTimes::getSet, R.color.bg_set),
    NADIR(SunTimes::getNadir, R.color.bg_nadir);

    private final Function<SunTimes, Date> dateExtractor;
    private final int colorResource;

    Type(Function<SunTimes, Date> dateExtractor, int colorResource) {
      this.dateExtractor = dateExtractor;
      this.colorResource = colorResource;
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

    public int getColorResource() {
      return colorResource;
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
