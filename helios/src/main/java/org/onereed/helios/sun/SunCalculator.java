package org.onereed.helios.sun;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.onereed.helios.common.LogUtil;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Obtains sun position info and hands it off to a consumer. */
public class SunCalculator {

  private static final String TAG = LogUtil.makeTag(SunCalculator.class);

  /**
   * How far in the past to start the "early" calculation.
   */
  private static final Duration SUN_TIMES_EARLY_OFFSET = Duration.ofHours(14L);

  /**
   * How far in the future to start the "later" calculation.
   */
  private static final Duration SUN_TIMES_LATER_OFFSET = Duration.ofHours(3L);

  private final Executor executor = Executors.newSingleThreadExecutor();

  private final Consumer<SunInfo> sunInfoConsumer;
  private final Clock clock;

  public SunCalculator(Consumer<SunInfo> sunInfoConsumer, Clock clock) {
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

  /**
   * A bug in {@link SunTimes} forces us to use a weird previous-time window, deduplicate events
   * from the two windows, and be tolerant of null noon and nadir values. See
   * https://github.com/shred/commons-suncalc/issues/18
   */
  private SunInfo getSunInfo(double lat, double lon, Instant now) {
    Log.d(TAG, String.format("lat=%f lon=%f now=%s", lat, lon, now));

    Date earlyTime = Date.from(now.minus(SUN_TIMES_EARLY_OFFSET));
    Date laterTime = Date.from(now.plus(SUN_TIMES_LATER_OFFSET));

    SunTimes earlySunTimes = SunTimes.compute().at(lat, lon).on(earlyTime).execute();
    SunTimes laterSunTimes = SunTimes.compute().at(lat, lon).on(laterTime).execute();

    boolean isCrossingHorizon = !(laterSunTimes.isAlwaysDown() || laterSunTimes.isAlwaysUp());

    Set<SunEvent> allSunEventsSet = new HashSet<>();
    allSunEventsSet.addAll(toSunEvents(earlySunTimes));
    allSunEventsSet.addAll(toSunEvents(laterSunTimes));

    ImmutableList<SunEvent> shownSunEvents = getShownEvents(allSunEventsSet, now);

    if (shownSunEvents.size() < 2) {
      return SunInfo.create(now, shownSunEvents, -1, isCrossingHorizon);
    }

    Duration beforeToNow = Duration.between(shownSunEvents.get(0).getTime(), now);
    Duration nowToAfter = Duration.between(now, shownSunEvents.get(1).getTime());
    int indexOfClosestEvent = beforeToNow.compareTo(nowToAfter) < 0 ? 0 : 1;

    return SunInfo.create(now, shownSunEvents, indexOfClosestEvent, isCrossingHorizon);
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes) {
    ImmutableList.Builder<SunEvent> builder = ImmutableList.builder();

    for (SunEvent.Type type : SunEvent.Type.values()) {
      Date date = type.getDate(sunTimes);
      if (date != null) {
        builder.add(SunEvent.create(date.toInstant(), type));
      }
    }

    return builder.build();
  }

  /**
   * We show the most recent previous event, the next upcoming event, and up to three more.
   */
  private ImmutableList<SunEvent> getShownEvents(Set<SunEvent> allSunEventsSet, Instant now) {
    ImmutableList<SunEvent> allSunEvents = ImmutableList.sortedCopyOf(allSunEventsSet);

    int firstNowOrAfter =
        Iterables.indexOf(allSunEvents, sunEvent -> !sunEvent.getTime().isBefore(now));

    if (firstNowOrAfter < 1) {
      Log.e(TAG, String.format("Now not in range. allSunEvents=%s now=%s", allSunEvents, now));
      return ImmutableList.of();
    }

    int firstBefore = firstNowOrAfter - 1;
    int showableEventCount = allSunEvents.size() - firstBefore;
    int last = firstBefore + Math.min(5, showableEventCount);

    return allSunEvents.subList(firstBefore, last);
  }
}
