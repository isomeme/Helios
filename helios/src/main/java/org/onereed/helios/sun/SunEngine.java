package org.onereed.helios.sun;

import android.annotation.SuppressLint;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Obtains sun position info and hands it off to a consumer.
 */
public class SunEngine {

  private static final String TAG = LogUtil.makeTag(SunEngine.class);

  private static final Duration ONE_DAY = Duration.ofDays(1L);

  private final Executor executor = Executors.newSingleThreadExecutor();

  private final Consumer<SunInfo> sunInfoConsumer;
  private final Clock clock;

  public SunEngine(Consumer<SunInfo> sunInfoConsumer, Clock clock) {
    this.sunInfoConsumer = sunInfoConsumer;
    this.clock = clock;
  }

  public void acceptLocation(@NonNull Location location) {
    executor.execute(() -> locationToSunInfo(location));
  }

  private void locationToSunInfo(@NonNull Location location) {
    double lat = location.getLatitude();
    double lon = location.getLongitude();
    Instant now = clock.instant();
    SunInfo sunInfo = getSunInfo(lat, lon, now);

    sunInfoConsumer.accept(sunInfo);
  }

  @SuppressLint("DefaultLocale")
  @VisibleForTesting
  static SunInfo getSunInfo(double lat, double lon, Instant now) {
    AppLogger.debug(TAG, String.format("lat=%f lon=%f now=%s", lat, lon, now));

    Date nextDay = Date.from(now);

    SunTimes nextSunTimes = SunTimes.compute().at(lat, lon).on(nextDay).execute();
    boolean isCrossingHorizon = !(nextSunTimes.isAlwaysDown() || nextSunTimes.isAlwaysUp());
    ImmutableList<SunEvent> nextEvents = toSunEvents(nextSunTimes);

    if (nextEvents.isEmpty()) {
      AppLogger.error(TAG, "Bad sun data, nextSunTimes=" + nextSunTimes);
      return SunInfo.EMPTY;
    }

    SunEvent nextEvent = nextEvents.get(0);
    Date precedingDay = Date.from(nextEvent.getTime().minus(ONE_DAY));
    SunTimes precedingSunTimes = SunTimes.compute().at(lat, lon).on(precedingDay).execute();
    ImmutableList<SunEvent> precedingEvents = toSunEvents(precedingSunTimes);

    if (precedingEvents.isEmpty()) {
      AppLogger.error(TAG, "Bad sun data, precedingSunTimes=" + precedingSunTimes);
      return SunInfo.EMPTY;
    }

    ImmutableList<SunEvent> shownSunEvents = getShownEvents(precedingEvents, nextEvents, now);

    Duration beforeToNow = Duration.between(shownSunEvents.get(0).getTime(), now);
    Duration nowToAfter = Duration.between(now, shownSunEvents.get(1).getTime());
    int indexOfClosestEvent = beforeToNow.compareTo(nowToAfter) < 0 ? 0 : 1;

    return SunInfo.create(now, shownSunEvents, indexOfClosestEvent, isCrossingHorizon);
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes) {
    List<SunEvent> unsortedEvents = new ArrayList<>();

    for (SunEvent.Type type : SunEvent.Type.values()) {
      Date date = type.getDate(sunTimes);
      if (date != null) {
        unsortedEvents.add(SunEvent.create(date.toInstant(), type));
      }
    }

    return ImmutableList.sortedCopyOf(unsortedEvents);
  }

  /** We show the most recent previous event and up to 4 upcoming events. */
  private static ImmutableList<SunEvent> getShownEvents(
      ImmutableList<SunEvent> precedingEvents, ImmutableList<SunEvent> nextEvents, Instant now) {

    // It is possible for the last preceding event and the first next event to be the same.
    // For example, during spring sunset happens several minutes later each day. If we run
    // between noon and sunset, the preceding day from the next sunset won't quite stretch
    // back far enough to reach yesterday's sunset time, so today's will be included instead.
    // We deal with this by filtering events that are not before 'now' from the preceding list.

    ImmutableList<SunEvent> validPrecedingEvents =
        precedingEvents.stream()
            .filter(event -> event.getTime().isBefore(now))
            .collect(toImmutableList());

    return ImmutableList.<SunEvent>builder()
        .add(Iterables.getLast(validPrecedingEvents))
        .addAll(Iterables.limit(nextEvents, 4))
        .build();
  }
}
