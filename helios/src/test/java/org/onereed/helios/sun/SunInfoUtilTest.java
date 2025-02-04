package org.onereed.helios.sun;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onereed.helios.common.Place;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent.Type;

/** Tests for {@link SunInfoUtil}. */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SunInfoUtilTest {

  // Coords for Santa Monica CA USA
  private static final Place PLACE = Place.of(34.0, -118.5, 0.0);

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
    SunInfo sunInfo = SunInfoUtil.getSunInfo(PLACE, when);

    SunInfo expectedSunInfo =
        SunInfo.builder()
            .setTimestamp(when)
            .setSunAzimuthInfo(
                SunAzimuthInfo.create(289.93357351191065f, true))
            .setMagneticDeclinationDeg(0.0)
            .setClosestEventIndex(1)
            .setSunEvents(
                event("2020-05-08T19:50:33Z", Type.NOON, 180.0486292899211),
                event("2020-05-09T02:43:51Z", Type.SET, 291.8290562827426),
                event("2020-05-09T07:50:18Z", Type.NADIR, 359.9482197950236),
                event("2020-05-09T12:56:55Z", Type.RISE, 68.05589238252242),
                event("2020-05-09T19:50:31Z", Type.NOON, 180.05583201838022))
            .build();

    assertEquals(expectedSunInfo, sunInfo);
  }

  /**
   * Make sure that we don't miss an event that we're right on top of by excluding it from both
   * preceding and following sets.
   */
  @Test
  public void testEventGap() {
    Instant when = Instant.parse("2020-05-18T02:50:50Z");
    SunInfo sunInfo = SunInfoUtil.getSunInfo(PLACE, when);

    SunInfo expectedSunInfo =
        SunInfo.builder()
            .setTimestamp(when)
            .setSunAzimuthInfo(
                SunAzimuthInfo.create(294.5638405069754f, true))
            .setMagneticDeclinationDeg(0.0)
            .setClosestEventIndex(0)
            .setSunEvents(
                event("2020-05-18T02:50:35Z", Type.SET, 294.52853932175793),
                event("2020-05-18T07:50:21Z", Type.NADIR, 359.9604617342177),
                event("2020-05-18T12:50:06Z", Type.RISE, 65.35870558986005),
                event("2020-05-18T19:50:33Z", Type.NOON, 180.03983395176928),
                event("2020-05-19T02:51:19Z", Type.SET, 294.79749942898354))
            .build();

    assertEquals(expectedSunInfo, sunInfo);
  }

  private static SunEvent event(String whenText, Type type, double azimuth) {
    Instant when = Instant.parse(whenText);
    return SunEvent.create(type, when, azimuth);
  }
}
