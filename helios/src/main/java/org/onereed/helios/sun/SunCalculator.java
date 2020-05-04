package org.onereed.helios.sun;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.onereed.helios.common.LogUtil;
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

import static java.util.Objects.requireNonNull;

/** Obtains sun position info and hands it off to a consumer. */
public class SunCalculator {

  private static final String TAG = LogUtil.makeTag(SunCalculator.class);

  private static final Duration ONE_DAY = Duration.ofDays(1);

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

  private SunInfo getSunInfo(double lat, double lon, Instant now) {
    Date nextTime = Date.from(now);
    Date prevTime = Date.from(now.minus(ONE_DAY));

    SunTimes nextSunTimes = SunTimes.compute().at(lat, lon).on(nextTime).fullCycle().execute();
    SunTimes prevSunTimes = SunTimes.compute().at(lat, lon).on(prevTime).fullCycle().execute();

    SunEvent mostRecentSunEvent = Iterables.getLast(toSunEvents(prevSunTimes));
    ImmutableList<SunEvent> upcomingSunEvents = toSunEvents(nextSunTimes);

    ImmutableList<SunEvent> allSunEvents =
        ImmutableList.<SunEvent>builder()
            .add(mostRecentSunEvent)
            .addAll(upcomingSunEvents)
            .build();

    Duration prevToNow = Duration.between(mostRecentSunEvent.getTime(), now);
    Duration nowToNext = Duration.between(now, upcomingSunEvents.get(0).getTime());
    int indexOfClosestEvent = prevToNow.compareTo(nowToNext) < 0 ? 0 : 1;

    boolean isCrossingHorizon = !(nextSunTimes.isAlwaysDown() || nextSunTimes.isAlwaysUp());

    return SunInfo.create(now, allSunEvents, indexOfClosestEvent, isCrossingHorizon);
  }

  private static ImmutableList<SunEvent> toSunEvents(SunTimes sunTimes) {
    List<SunEvent> sunEvents = new ArrayList<>();

    sunEvents.add(SunEvent.create(sunTimes.getNadir().toInstant(), SunEvent.Type.NADIR));
    sunEvents.add(SunEvent.create(sunTimes.getNoon().toInstant(), SunEvent.Type.NOON));

    if (!sunTimes.isAlwaysDown() && !sunTimes.isAlwaysUp()) {
      sunEvents.add(
          SunEvent.create(requireNonNull(sunTimes.getRise()).toInstant(), SunEvent.Type.RISE));
      sunEvents.add(
          SunEvent.create(requireNonNull(sunTimes.getSet()).toInstant(), SunEvent.Type.SET));
    }

    return ImmutableList.sortedCopyOf(sunEvents);
  }
}
