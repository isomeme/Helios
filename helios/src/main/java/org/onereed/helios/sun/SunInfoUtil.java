package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import org.onereed.helios.common.FormattedVerifyException;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.reverseOrder;

/** Implements a static method which produces {@link SunInfo} instances. */
class SunInfoUtil {

  private static final String TAG = LogUtil.makeTag(SunInfoUtil.class);

  private static final Duration ONE_DAY = Duration.ofDays(1L);

  static @NonNull SunInfo getSunInfo(double lat, double lon, @NonNull Instant when) {
    AppLogger.debug(TAG, "lat=%.3f lon=%.3f when=%s", lat, lon, when);

    Date nextDay = Date.from(when);
    SunTimes nextSunTimes = SunTimes.compute().at(lat, lon).on(nextDay).execute();
    boolean isCrossingHorizon = !(nextSunTimes.isAlwaysDown() || nextSunTimes.isAlwaysUp());
    ImmutableList<SunEvent> nextEvents = toSunEvents(nextSunTimes);

    verify(!nextEvents.isEmpty(), "nextEvents empty for nextSunTimes=%s", nextSunTimes);

    SunEvent nextEvent = nextEvents.get(0);
    Date precedingDay = Date.from(nextEvent.getTime().minus(ONE_DAY));
    SunTimes precedingSunTimes = SunTimes.compute().at(lat, lon).on(precedingDay).execute();
    ImmutableList<SunEvent> precedingEvents = toSunEvents(precedingSunTimes);

    verify(
        !precedingEvents.isEmpty(), "precedingEvents empty for precedingSunTimes=%s", nextSunTimes);

    ImmutableList<SunEvent> shownSunEvents = getShownEvents(precedingEvents, nextEvents, when);

    Instant beforeTime = shownSunEvents.get(0).getTime();
    Instant afterTime = shownSunEvents.get(1).getTime();
    Duration between = Duration.between(beforeTime, afterTime);
    Instant halfway = beforeTime.plus(between.dividedBy(2L));

    int indexOfClosestEvent;
    Instant staleTime;

    if (when.isBefore(halfway)) {
      indexOfClosestEvent = 0;
      staleTime = halfway;
    } else {
      indexOfClosestEvent = 1;
      staleTime = afterTime;
    }

    return SunInfo.create(when, shownSunEvents, indexOfClosestEvent, isCrossingHorizon, staleTime);
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes) {
    return Arrays.stream(SunEvent.Type.values())
        .map(type -> type.createSunEvent(sunTimes))
        .flatMap(Streams::stream)
        .sorted()
        .collect(toImmutableList());
  }

  /** We show the most recent previous event and up to 4 upcoming events. */
  private static ImmutableList<SunEvent> getShownEvents(
      ImmutableList<SunEvent> precedingEvents, ImmutableList<SunEvent> nextEvents, Instant when) {

    // It is possible for the last preceding event and the first next event to be the same.
    // For example, during spring sunset happens several minutes later each day. If we run
    // between noon and sunset, the preceding day from the next sunset won't quite stretch
    // back far enough to reach yesterday's sunset time, so today's will be included instead.
    // We deal with this by filtering events that are not before 'when' from the preceding list.
    // To guard against weird polar edge cases, we actually allow for an overlap of more than one
    // preceding item.

    SunEvent lastPrecedingEvent =
        precedingEvents.stream()
            .sorted(reverseOrder())
            .filter(event -> event.getTime().isBefore(when))
            .findFirst()
            .orElseThrow(
                () ->
                    new FormattedVerifyException(
                        "Nothing in precedingEvents=%s is before when=%s", precedingEvents, when));

    return ImmutableList.<SunEvent>builder()
        .add(lastPrecedingEvent)
        .addAll(Iterables.limit(nextEvents, 4))
        .build();
  }

  private SunInfoUtil() {}
}
