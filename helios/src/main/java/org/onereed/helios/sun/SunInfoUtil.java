package org.onereed.helios.sun;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import org.onereed.helios.common.FormattedVerifyException;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.reverseOrder;

/** Implements a static method which produces {@link SunInfo} instances. */
class SunInfoUtil {

  private static final String TAG = LogUtil.makeTag(SunInfoUtil.class);

  /**
   * We use an offset of just less than a full day to get the preceding events, to avoid edge cases
   * where an event happening right at the time we're checking falls out of both the preceding and
   * upcoming events lists. We then remove duplicates in the preceding events.
   */
  private static final Duration PRECEDING_OFFSET = Duration.ofDays(1L).minusMinutes(30L);

  /**
   * We're forced to run SunCalc in 'fullCycle' mode to avoid missing e.g. a sunset that happens 24
   * hrs 1 minute from now. But we don't want to confuse the display by showing a sunset that won't
   * happen for a long time (e.g. in arctic summer. We throw away events more than this far in the
   * future.
   */
  private static final Duration FUTURE_EVENT_WINDOW = Duration.ofHours(36L);

  static @NonNull SunInfo getSunInfo(@NonNull LatLon latLon, @NonNull Instant when) {
    AppLogger.debug(TAG, "latLon=%s when=%s", latLon, when);

    SunTimes nextSunTimes = getSunTimes(latLon, when);
    ImmutableList<SunEvent> nextEvents = toSunEvents(nextSunTimes, latLon, when);

    verify(!nextEvents.isEmpty(), "nextEvents empty for nextSunTimes=%s", nextSunTimes);

    SunEvent nextEvent = nextEvents.get(0);
    Instant precedingDay = nextEvent.getTime().minus(PRECEDING_OFFSET);
    SunTimes precedingSunTimes = getSunTimes(latLon, precedingDay);
    ImmutableList<SunEvent> precedingEvents = toSunEvents(precedingSunTimes, latLon, when);

    verify(
        !precedingEvents.isEmpty(), "precedingEvents empty for precedingSunTimes=%s", nextSunTimes);

    SunEvent mostRecentEvent = getMostRecentEvent(precedingEvents, nextEvents);
    Iterable<SunEvent> eventsAfterNext = Iterables.skip(nextEvents, 1);

    Instant beforeTime = mostRecentEvent.getTime();
    Instant afterTime = nextEvent.getTime();
    Duration between = Duration.between(beforeTime, afterTime);
    Instant halfway = beforeTime.plus(between.dividedBy(2L));
    int closestEventIndex = when.isBefore(halfway) ? 0 : 1;

    ImmutableList<SunEvent> shownSunEvents =
        ImmutableList.<SunEvent>builder()
            .add(mostRecentEvent)
            .add(nextEvent)
            .addAll(eventsAfterNext)
            .build();

    double currentSunAzimuth =
        SunPosition.compute().at(latLon.asArray()).on(when).execute().getAzimuth();

    return SunInfo.create(when, currentSunAzimuth, closestEventIndex, shownSunEvents);
  }

  private static SunTimes getSunTimes(LatLon latLon, Instant when) {
    // Full-cycle mode is needed when the current time is just after an event -- sunset, for
    // example -- and the next such event is more than 24 hours in the future, as happens throughout
    // winter and spring.

    return SunTimes.compute().at(latLon.asArray()).on(when).fullCycle().execute();
  }

  private static ImmutableList<SunEvent> toSunEvents(
      SunTimes sunTimes, LatLon latLon, Instant when) {

    Instant futureEventLimit = when.plus(FUTURE_EVENT_WINDOW);

    return Arrays.stream(SunEvent.Type.values())
        .map(type -> type.createSunEvent(sunTimes, latLon))
        .flatMap(Streams::stream)
        .filter(event -> event.getTime().isBefore(futureEventLimit))
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
