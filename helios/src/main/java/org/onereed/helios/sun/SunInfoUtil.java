package org.onereed.helios.sun;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.reverseOrder;

import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.FormattedVerifyException;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.Place;
import org.onereed.helios.logger.AppLogger;
import org.shredzone.commons.suncalc.SunTimes;

/** Implements a static method which produces {@link SunInfo} instances. */
class SunInfoUtil {

  private static final String TAG = LogUtil.makeTag(SunInfoUtil.class);

  /**
   * We use this value combined with {@link #PRECEDING_LIMIT} to include "preceding" events up to
   * and beyond the next upcoming event, to avoid edge cases where an event happening near the time
   * we're checking falls out of both the preceding and upcoming events lists. We then remove
   * duplicates in the preceding events.
   */
  private static final Duration PRECEDING_OFFSET = Duration.ofHours(13L);

  /** See {@link #PRECEDING_OFFSET}. */
  private static final Duration PRECEDING_LIMIT = Duration.ofHours(14L);

  /**
   * We want to display e.g. a sunset that happens 24 hrs 1 minute from now. But we don't want to
   * confuse the display by showing a sunset that won't happen for a long time (e.g. in arctic
   * summer). We limit the search for future events to this long from now.
   */
  private static final Duration FUTURE_LIMIT = Duration.ofHours(36L);

  static @NonNull SunInfo getSunInfo(@NonNull Place where, @NonNull Instant when) {
    AppLogger.debug(TAG, "where=%s when=%s", where, when);

    SunTimes nextSunTimes = SunCalcUtil.getSunTimes(where, when, FUTURE_LIMIT);
    ImmutableList<SunEvent> nextEvents = toSunEvents(nextSunTimes, where);

    verify(!nextEvents.isEmpty(), "nextEvents empty for nextSunTimes=%s", nextSunTimes);

    SunEvent nextEvent = nextEvents.get(0);
    Instant precedingTime = nextEvent.getTime().minus(PRECEDING_OFFSET);
    SunTimes precedingSunTimes = SunCalcUtil.getSunTimes(where, precedingTime, PRECEDING_LIMIT);
    ImmutableList<SunEvent> precedingEvents = toSunEvents(precedingSunTimes, where);

    verify(
        !precedingEvents.isEmpty(), "precedingEvents empty for precedingSunTimes=%s", nextSunTimes);

    SunEvent mostRecentEvent = getMostRecentEvent(precedingEvents, nextEvent);
    int closestEventIndex = getClosestEventIndex(when, mostRecentEvent, nextEvent);

    ImmutableList<SunEvent> shownSunEvents =
        ImmutableList.<SunEvent>builder().add(mostRecentEvent).addAll(nextEvents).build();

    SunAzimuthInfo sunAzimuthInfo = SunAzimuthInfo.from(where, when);
    double magneticDeclinationDeg = DirectionUtil.getMagneticDeclinationDeg(where, when);

    return SunInfo.builder()
        .setTimestamp(when)
        .setSunAzimuthInfo(sunAzimuthInfo)
        .setMagneticDeclinationDeg(magneticDeclinationDeg)
        .setClosestEventIndex(closestEventIndex)
        .setSunEvents(shownSunEvents)
        .build();
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes, Place where) {
    return Arrays.stream(SunEvent.Type.values())
        .map(type -> type.createSunEvent(sunTimes, where))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted()
        .collect(toImmutableList());
  }

  private static SunEvent getMostRecentEvent(
      ImmutableList<SunEvent> precedingEvents, SunEvent nextEvent) {

    return precedingEvents.stream()
        .sorted(reverseOrder())
        .filter(event -> !event.getType().equals(nextEvent.getType()))
        .filter(event -> event.getTime().isBefore(nextEvent.getTime()))
        .findFirst()
        .orElseThrow(
            () ->
                new FormattedVerifyException(
                    "Nothing in precedingEvents=%s works as a preceding event for nextEvent=%s",
                    precedingEvents, nextEvent));
  }

  private static int getClosestEventIndex(
      Instant when, SunEvent mostRecentEvent, SunEvent nextEvent) {

    Instant beforeTime = mostRecentEvent.getTime();
    Instant afterTime = nextEvent.getTime();
    Duration between = Duration.between(beforeTime, afterTime);
    Instant halfway = beforeTime.plus(between.dividedBy(2L));
    return when.isBefore(halfway) ? 0 : 1;
  }

  private SunInfoUtil() {}
}
