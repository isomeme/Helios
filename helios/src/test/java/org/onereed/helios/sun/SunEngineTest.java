package org.onereed.helios.sun;

import android.location.Location;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onereed.helios.logger.AppLogger;

import java.time.Clock;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SunEngine}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SunEngineTest {

  // Coords for Santa Monica CA USA
  private static final double LAT = 34.0;
  private static final double LON = -118.5;

  @Mock
  private Clock mockClock;
  @Mock
  private Location mockLocation;

  private SunEngine sunEngine;

  @Before
  public void setup() {
    AppLogger.useJavaLogger();
    sunEngine = new SunEngine(mockClock);
  }

  /**
   * Make sure we don't use a "preceding" event in the future. In this case, the same sunset appears
   * at the end of the old data and the beginning of the new data.
   */
  @Test
  public void testEventOverlap() {
    Instant now = Instant.parse("2020-05-09T02:30:00Z");
    when(mockClock.instant()).thenReturn(now);
    when(mockLocation.getLatitude()).thenReturn(LAT);
    when(mockLocation.getLongitude()).thenReturn(LON);

    SunInfo sunInfo = sunEngine.locationToSunInfo(mockLocation);

    SunInfo expectedSunInfo =
        SunInfo.create(
            now,
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
