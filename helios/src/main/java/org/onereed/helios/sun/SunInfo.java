package org.onereed.helios.sun;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Instant;

/**
 * Information about the most recent sun event, and upcoming ones.
 */
@AutoValue
public abstract class SunInfo {

  public static final SunInfo EMPTY =
      SunInfo.create(Instant.EPOCH, ImmutableList.of(), -1, true, Instant.EPOCH);

  public abstract Instant getTimestamp();

  public abstract ImmutableList<SunEvent> getSunEvents();

  public abstract int getIndexOfClosestEvent();

  public abstract boolean isCrossingHorizon();

  public abstract Instant getStaleTime();

  static SunInfo create(
      Instant timestamp,
      ImmutableList<SunEvent> sunEvents,
      int indexOfClosestEvent,
      boolean isCrossingHorizon,
      Instant staleTime) {

    return new AutoValue_SunInfo(timestamp, sunEvents, indexOfClosestEvent, isCrossingHorizon,
        staleTime);
  }
}
