package org.onereed.helios.sun;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent.Type;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

/** Tests for {@link SunInfoUtil}. */
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
    Instant when = Instant.parse("2020-05-09T02:30:15Z");
    SunInfo sunInfo = SunInfoUtil.getSunInfo(LAT, LON, when);

    SunInfo expectedSunInfo =
        SunInfo.create(
            when,
            1,
            ImmutableList.of(
                event("2020-05-08T19:52:00Z", Type.NOON, 181.25805009888),
                event("2020-05-09T02:44:00Z", Type.SET, 291.8502225919628),
                event("2020-05-09T07:50:00Z", Type.NADIR, 359.8568414354525),
                event("2020-05-09T12:57:00Z", Type.RISE, 68.06762096768347),
                event("2020-05-09T19:47:00Z", Type.NOON, 177.0827771863477)));

    assertEquals(expectedSunInfo, sunInfo);
  }

  /**
   * Make sure that we don't miss an event that we're right on top of by excluding it from both
   * preceding and following sets.
   */
  @Test
  public void testEventGap() {
    Instant when = Instant.parse("2020-05-18T02:50:50Z");
    SunInfo sunInfo = SunInfoUtil.getSunInfo(LAT, LON, when);

    SunInfo expectedSunInfo =
        SunInfo.create(
            when,
            0,
            ImmutableList.of(
                event("2020-05-18T02:51:00Z", Type.SET, 294.5873847123942),
                event("2020-05-18T07:50:00Z", Type.NADIR, 359.8582061019339),
                event("2020-05-18T12:50:00Z", Type.RISE, 65.34461082274493),
                event("2020-05-18T19:51:00Z", Type.NOON, 180.47091858781326),
                event("2020-05-19T02:51:00Z", Type.SET, 294.7528076783142)));

    assertEquals(expectedSunInfo, sunInfo);
  }

  private static SunEvent event(String whenText, Type type, double azimuth) {
    Instant when = Instant.parse(whenText);
    return SunEvent.create(when, type, azimuth);
  }
}
