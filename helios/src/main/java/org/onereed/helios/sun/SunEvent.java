package org.onereed.helios.sun;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.auto.value.AutoValue;

import org.onereed.helios.common.Place;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/** Represents one sun event -- rise, noon, set, or nadir. */
@AutoValue
public abstract class SunEvent implements Comparable<SunEvent> {

  private static final Comparator<SunEvent> COMPARATOR =
      Comparator.comparing(SunEvent::getTime).thenComparing(SunEvent::getType);

  public enum Type {
    RISE(SunTimes::getRise),
    NOON(SunTimes::getNoon),
    SET(SunTimes::getSet),
    NADIR(SunTimes::getNadir);

    private final Function<SunTimes, ZonedDateTime> dateExtractor;

    Type(Function<SunTimes, ZonedDateTime> dateExtractor) {
      this.dateExtractor = dateExtractor;
    }

    /**
     * Returns the {@link SunEvent} corresponding to this {@link Type}, in the given {@link
     * SunTimes} instance, if it is available. Rise and set events will not be available for arctic
     * summer and winter.
     */
    Optional<SunEvent> createSunEvent(@NonNull SunTimes sunTimes, @NonNull Place where) {
      return Optional.ofNullable(dateExtractor.apply(sunTimes))
          .map(ZonedDateTime::toInstant)
          .map(when -> SunEvent.create(when, this, SunCalcUtil.getSunAzimuthDeg(where, when)));
    }
  }

  @VisibleForTesting
  static SunEvent create(Instant time, Type type, double azimuthDeg) {
    return new AutoValue_SunEvent(time, type, azimuthDeg);
  }

  @NonNull
  public abstract Instant getTime();

  @NonNull
  public abstract Type getType();

  public abstract double getAzimuthDeg();

  @Override
  public int compareTo(@NonNull SunEvent o) {
    return COMPARATOR.compare(this, o);
  }
}
