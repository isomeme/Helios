package org.onereed.helios.sun;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Instant;

/** Information about the most recent sun event, and upcoming ones. */
@AutoValue
public abstract class SunInfo {

  public abstract Instant getTimestamp();

  public abstract float getSunAzimuthDeg();

  public abstract double getMagneticDeclinationDeg();

  public abstract int getClosestEventIndex();

  public abstract ImmutableList<SunEvent> getSunEvents();

  public static Builder builder() {
    return new AutoValue_SunInfo.Builder();
  }

  /** Builder for {@link SunInfo} */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTimestamp(Instant timestamp);

    public abstract Builder setSunAzimuthDeg(float sunAzimuthDeg);

    public abstract Builder setMagneticDeclinationDeg(double magneticDeclinationDeg);

    public abstract Builder setClosestEventIndex(int closestEventIndex);

    public abstract Builder setSunEvents(ImmutableList<SunEvent> sunEvents);

    public abstract Builder setSunEvents(SunEvent... sunEvents);

    public Builder setSunAzimuthDeg(double sunAzimuthDeg) {
      return setSunAzimuthDeg((float) sunAzimuthDeg);
    }

    public abstract SunInfo build();
  }
}
