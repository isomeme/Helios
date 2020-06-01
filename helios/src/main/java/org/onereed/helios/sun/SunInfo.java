package org.onereed.helios.sun;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Instant;

/** Information about the most recent sun event, and upcoming ones. */
@AutoValue
public abstract class SunInfo {

  public abstract Instant getTimestamp();

  public abstract int closestEventIndex();

  public abstract ImmutableList<SunEvent> getSunEvents();

  static SunInfo create(
      Instant timestamp, int closestEventIndex, ImmutableList<SunEvent> sunEvents) {

    return new AutoValue_SunInfo(timestamp, closestEventIndex, sunEvents);
  }
}
