package org.onereed.helios.sun;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Instant;

/** Information about the most recent sun event, and upcoming ones. */
@AutoValue
public abstract class SunInfo {

  public enum HorizonStatus {
    NORMAL,
    ALWAYS_ABOVE,
    ALWAYS_BELOW
  }

  public abstract Instant getTimestamp();

  public abstract HorizonStatus getHorizonStatus();

  public abstract ImmutableList<SunEvent> getSunEvents();

  static SunInfo create(
      Instant timestamp, HorizonStatus horizonStatus, ImmutableList<SunEvent> sunEvents) {

    return new AutoValue_SunInfo(timestamp, horizonStatus, sunEvents);
  }
}
