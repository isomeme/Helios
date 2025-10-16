package org.onereed.helios.sun;

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.Place;
import org.shredzone.commons.suncalc.SunTimes;

/** Represents one sun event -- rise, noon, set, or nadir. */
@AutoValue
public abstract class SunEvent implements Comparable<SunEvent> {

  /**
   * Epoch seconds divided by this value yields a time bucket within which two events with different
   * times and the same {@link Type} might actually be the same event.
   */
  private static final long EVENT_TIME_BUCKET_SIZE_SEC = Duration.ofHours(4L).getSeconds();

  /**
   * The ordinal of the this event's {@link Type} is multiplied by this value before being added to
   * the time bucket to yield a weak event ID. This must be >> than the largest expected time bucket
   * value.
   */
  private static final long TYPE_ORDINAL_SCALE = 10_000_000L;

  private static final Comparator<SunEvent> COMPARATOR =
      Comparator.comparing(SunEvent::getTime).thenComparing(SunEvent::getType);

  public enum Type {
    RISE(SunTimes::getRise),
    NOON(SunTimes::getNoon),
    SET(SunTimes::getSet),
    NADIR(SunTimes::getNadir);

    private final Function<SunTimes, ZonedDateTime> timeExtractor;

    Type(Function<SunTimes, ZonedDateTime> timeExtractor) {
      this.timeExtractor = timeExtractor;
    }

    /**
     * Returns the {@link SunEvent} corresponding to this {@link Type}, in the given {@link
     * SunTimes} instance, if it is available. Rise and set events will not be available for arctic
     * summer and winter.
     */
    Optional<SunEvent> createSunEvent(@NonNull SunTimes sunTimes, @NonNull Place where) {
      return Optional.ofNullable(timeExtractor.apply(sunTimes))
          .map(ZonedDateTime::toInstant)
          .map(
              when ->
                  SunEvent.create(
                      this, when, where.asPositionParameters().on(when).execute().getAzimuth()));
    }
  }

  static SunEvent create(Type type, Instant time, double azimuthDeg) {
    return new AutoValue_SunEvent(type, time, (float) azimuthDeg, computeWeakId(type, time));
  }

  private static long computeWeakId(Type type, Instant time) {
    long timeBucket = time.getEpochSecond() / EVENT_TIME_BUCKET_SIZE_SEC;
    long ordinalOffset = TYPE_ORDINAL_SCALE * type.ordinal();
    return timeBucket + ordinalOffset;
  }

  @NonNull
  public abstract Type getType();

  @NonNull
  public abstract Instant getTime();

  /** Sun azimuth degrees, clockwise from north, [0..360). */
  public abstract double getAzimuthDeg();

  /**
   * The {@link androidx.recyclerview.widget.RecyclerView} we use to display the list of sun events can do some nice transition
   * animations if it can identify which cards are the same after a data update. This method returns
   * an ID value which is likely to remain stable under expected fluctuations in calculated event
   * times, and very unlikely to collide with the ID of a different event.
   */
  public abstract long getWeakId();

  @Override
  public int compareTo(@NonNull SunEvent o) {
    return COMPARATOR.compare(this, o);
  }

  public boolean isNear(SunEvent other) {
    return Math.abs(DirectionUtil.arc(getAzimuthDeg(), other.getAzimuthDeg())) < 20.0;
  }
}