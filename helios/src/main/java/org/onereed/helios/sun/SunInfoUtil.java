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

  /**
   * We use an offset of just less than a full day to get the preceding events, to avoid edge
   * cases where an event happening right at the time we're checking falls out of both the
   * preceding and upcoming events lists. We then remove duplicates in the preceding events.
   */
  private static final Duration PRECEDING_OFFSET = Duration.ofDays(1L).minusMinutes(30L);

  static @NonNull SunInfo getSunInfo(double lat, double lon, @NonNull Instant when) {
    AppLogger.debug(TAG, "lat=%.3f lon=%.3f when=%s", lat, lon, when);

    SunTimes nextSunTimes = getSunTimes(lat, lon, when);
    SunInfo.HorizonStatus horizonStatus = getHorizonStatus(nextSunTimes);
    ImmutableList<SunEvent> nextEvents = toSunEvents(nextSunTimes);

    verify(!nextEvents.isEmpty(), "nextEvents empty for nextSunTimes=%s", nextSunTimes);

    SunEvent nextEvent = nextEvents.get(0);
    Instant precedingDay = nextEvent.getTime().minus(PRECEDING_OFFSET);
    SunTimes precedingSunTimes = getSunTimes(lat, lon, precedingDay);
    ImmutableList<SunEvent> precedingEvents = toSunEvents(precedingSunTimes);

    verify(
        !precedingEvents.isEmpty(), "precedingEvents empty for precedingSunTimes=%s", nextSunTimes);

    SunEvent mostRecentEvent = getMostRecentEvent(precedingEvents, nextEvents);
    Iterable<SunEvent> eventsAfterNext = Iterables.skip(nextEvents, 1);

    Instant beforeTime = mostRecentEvent.getTime();
    Instant afterTime = nextEvent.getTime();
    Duration between = Duration.between(beforeTime, afterTime);
    Instant halfway = beforeTime.plus(between.dividedBy(2L));

    if (when.isBefore(halfway)) {
      mostRecentEvent = mostRecentEvent.toBuilder().setClosest(true).build();
    } else {
      nextEvent = nextEvent.toBuilder().setClosest(true).build();
    }

    ImmutableList<SunEvent> shownSunEvents =
        ImmutableList.<SunEvent>builder()
            .add(mostRecentEvent)
            .add(nextEvent)
            .addAll(eventsAfterNext)
            .build();

    return SunInfo.create(when, horizonStatus, shownSunEvents);
  }

  private static SunTimes getSunTimes(double lat, double lon, Instant when) {
    // Full-cycle mode is needed when the current time is just after an event -- sunset, for
    // example -- and the next such event is more than 24 hours in the future, as happens throughout
    // winter and spring.

    return SunTimes.compute().at(lat, lon).on(Date.from(when)).fullCycle().execute();
  }

  private static SunInfo.HorizonStatus getHorizonStatus(SunTimes nextSunTimes) {
    if (nextSunTimes.isAlwaysUp()) {
      return SunInfo.HorizonStatus.ALWAYS_ABOVE;
    }

    if (nextSunTimes.isAlwaysDown()) {
      return SunInfo.HorizonStatus.ALWAYS_BELOW;
    }

    return SunInfo.HorizonStatus.NORMAL;
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes) {
    return Arrays.stream(SunEvent.Type.values())
        .map(type -> type.createSunEvent(sunTimes))
        .flatMap(Streams::stream)
        .sorted()
        .collect(toImmutableList());
  }

  private static SunEvent getMostRecentEvent(
      ImmutableList<SunEvent> precedingEvents, ImmutableList<SunEvent> nextEvents) {

    return precedingEvents.stream()
        .sorted(reverseOrder())
        .filter(event -> nextEvents.stream().noneMatch(event::isDuplicateOf))
        .findFirst()
        .orElseThrow(
            () ->
                new FormattedVerifyException(
                    "Nothing in precedingEvents=%s is not also in nextEvents=%s",
                    precedingEvents, nextEvents));
  }

  private SunInfoUtil() {}
}
