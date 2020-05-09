package org.onereed.helios.sun;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SunEngine}.
 */
public class SunEngineTest {

  // Coords for Santa Monica CA USA
  private static final double LAT = 34.0;
  private static final double LON = -118.5;

  /**
   * Make sure we don't use a "preceding" event in the future.
   */
  @Test
  public void testEventOverlap() {
    Instant now = Instant.parse("2020-05-09T02:30:00Z");
    SunInfo sunInfo = SunEngine.getSunInfo(LAT, LON, now);

    SunInfo expectedSunInfo =
        SunInfo.create(
            Instant.parse("2020-05-09T02:30:00Z"),
            ImmutableList.of(
                SunEvent.create(Instant.parse("2020-05-08T19:50:00Z"), SunEvent.Type.NOON),
                SunEvent.create(Instant.parse("2020-05-09T02:44:00Z"), SunEvent.Type.SET),
                SunEvent.create(Instant.parse("2020-05-09T07:50:00Z"), SunEvent.Type.NADIR),
                SunEvent.create(Instant.parse("2020-05-09T12:57:00Z"), SunEvent.Type.RISE),
                SunEvent.create(Instant.parse("2020-05-09T19:47:00Z"), SunEvent.Type.NOON)),
            1,
            true);

    assertEquals(expectedSunInfo, sunInfo);
  }
}
