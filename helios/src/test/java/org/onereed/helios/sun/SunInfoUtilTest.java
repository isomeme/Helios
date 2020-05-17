package org.onereed.helios.sun;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onereed.helios.logger.AppLogger;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SunInfoUtil}.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SunInfoUtilTest {

  // Coords for Santa Monica CA USA
  private static final double LAT = 34.0;
  private static final double LON = -118.5;

  @Before
  public void setup() {
    AppLogger.useJavaLogger();
  }

  /**
   * Make sure we don't use a "preceding" event in the future. In this case, the same sunset appears
   * at the end of the old data and the beginning of the new data.
   */
  @Test
  public void testEventOverlap() {
    Instant when = Instant.parse("2020-05-09T02:30:00Z");
    Instant staleTime = Instant.parse("2020-05-09T02:44:00Z");
    SunInfo sunInfo = SunInfoUtil.getSunInfo(LAT, LON, when);

    SunInfo expectedSunInfo =
        SunInfo.create(
            when,
            ImmutableList.of(
                SunEvent.create(Instant.parse("2020-05-08T19:50:00Z"), SunEvent.Type.NOON),
                SunEvent.create(Instant.parse("2020-05-09T02:44:00Z"), SunEvent.Type.SET),
                SunEvent.create(Instant.parse("2020-05-09T07:50:00Z"), SunEvent.Type.NADIR),
                SunEvent.create(Instant.parse("2020-05-09T12:57:00Z"), SunEvent.Type.RISE),
                SunEvent.create(Instant.parse("2020-05-09T19:47:00Z"), SunEvent.Type.NOON)),
            1,
            true,
            staleTime);

    assertEquals(expectedSunInfo, sunInfo);
  }
}
