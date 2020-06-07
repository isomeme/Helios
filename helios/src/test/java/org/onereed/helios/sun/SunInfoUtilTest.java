package org.onereed.helios.sun;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onereed.helios.location.LatLon;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent.Type;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

/** Tests for {@link SunInfoUtil}. */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SunInfoUtilTest {

  // Coords for Santa Monica CA USA
  private static final LatLon LAT_LON = LatLon.of(34.0, -118.5);

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
    SunInfo sunInfo = SunInfoUtil.getSunInfo(LAT_LON, when);

    SunInfo expectedSunInfo =
        SunInfo.create(
            when,
            289.93357351191065,
            1,
            ImmutableList.of(
                event("2020-05-08T19:50:31Z", Type.NOON, 180.02082324789518),
                event("2020-05-09T02:43:51Z", Type.SET, 291.8290562827426),
                event("2020-05-09T07:50:20Z", Type.NADIR, 359.9583729494134),
                event("2020-05-09T12:56:55Z", Type.RISE, 68.05589238252242),
                event("2020-05-09T19:50:29Z", Type.NOON, 180.02763142069227)));

    assertEquals(expectedSunInfo, sunInfo);
  }

  /**
   * Make sure that we don't miss an event that we're right on top of by excluding it from both
   * preceding and following sets.
   */
  @Test
  public void testEventGap() {
    Instant when = Instant.parse("2020-05-18T02:50:50Z");
    SunInfo sunInfo = SunInfoUtil.getSunInfo(LAT_LON, when);

    SunInfo expectedSunInfo =
        SunInfo.create(
            when,
            294.5638405069754,
            0,
            ImmutableList.of(
                event("2020-05-18T02:50:41Z", Type.SET, 294.54265762125186),
                event("2020-05-18T07:50:21Z", Type.NADIR, 359.9604617342177),
                event("2020-05-18T12:50:06Z", Type.RISE, 65.35870558986005),
                event("2020-05-18T19:50:35Z", Type.NOON, 180.07176664466613),
                event("2020-05-19T02:51:19Z", Type.SET, 294.79749942898354)));

    assertEquals(expectedSunInfo, sunInfo);
  }

  private static SunEvent event(String whenText, Type type, double azimuth) {
    Instant when = Instant.parse(whenText);
    return SunEvent.create(when, type, azimuth);
  }
}
