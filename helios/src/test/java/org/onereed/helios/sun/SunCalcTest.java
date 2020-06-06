package org.onereed.helios.sun;

import org.junit.Test;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * A test originally written to show that {@link SunTimes} returned null noon and nadir values for
 * calculations done at {@link Date} values near these times. That bug has since been fixed, and the
 * test now verifies correct behavior.
 */
public class SunCalcTest {

  // All time and location parameters are for local solar noon in Santa Monica CA USA on
  // 2020-05-03 12:52:00 GMT-07:00

  private static final Instant NOON = Instant.parse("2020-05-03T19:51:00Z");
  private static final Instant NADIR = Instant.parse("2020-05-04T07:51:00Z");

  /** It happens that May 4 noon is at the exact same (minute-rounded) time as May 3. */
  private static final Instant NOON_TOMORROW = NOON.plus(Duration.ofDays(1L));

  private static final double[] COORDS = {34.0, -118.5};

  private static final Duration SHORT_DURATION = Duration.ofMinutes(2L);
  private static final Duration LONG_DURATION = Duration.ofMinutes(30L);

  @Test
  public void testWellBeforeNoon() {
    Date wellBeforeNoon = Date.from(NOON.minus(LONG_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(wellBeforeNoon).at(COORDS).execute();

    assertEquals(NOON, sunTimes.getNoon().toInstant());
  }

  @Test
  public void testJustBeforeNoon() {
    Date justBeforeNoon = Date.from(NOON.minus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justBeforeNoon).at(COORDS).execute();

    assertEquals(NOON, sunTimes.getNoon().toInstant());
  }

  @Test
  public void testJustAfterNoon() {
    Date justAfterNoon = Date.from(NOON.plus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justAfterNoon).at(COORDS).execute();

    assertEquals(NOON_TOMORROW, sunTimes.getNoon().toInstant());
  }

  @Test
  public void testWellAfterNoon() {
    Date wellAfterNoon = Date.from(NOON.plus(LONG_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(wellAfterNoon).at(COORDS).execute();

    assertEquals(NOON_TOMORROW, sunTimes.getNoon().toInstant());
  }

  @Test
  public void testJustBeforeNadir() {
    Date wellAfterNoon = Date.from(NOON.plus(LONG_DURATION));
    SunTimes sunTimes1 = SunTimes.compute().on(wellAfterNoon).at(COORDS).execute();
    Instant nadir = sunTimes1.getNadir().toInstant();
    Date justBeforeNadir = Date.from(nadir.minus(SHORT_DURATION));
    SunTimes sunTimes2 = SunTimes.compute().on(justBeforeNadir).at(COORDS).execute();

    assertEquals(NADIR, sunTimes2.getNadir().toInstant());
  }

  @Test
  public void testJustBeforeNoonFullCycle() {
    Date justBeforeNoon = Date.from(NOON.minus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justBeforeNoon).fullCycle().at(COORDS).execute();

    assertEquals(NOON, sunTimes.getNoon().toInstant());
  }

  private static final Date NOON_NADIR_BAD_TIME = Date.from(Instant.parse("2020-06-02T03:40:00Z"));

  @Test
  public void testAzimuthCloseNoon_bad() {
    SunTimes sunTimes = SunTimes.compute().at(COORDS).on(NOON_NADIR_BAD_TIME).execute();
    Date noon = sunTimes.getNoon();
    SunPosition sunPosition = SunPosition.compute().at(COORDS).on(noon).execute();

    // It seems reasonable to expect that northern temperate zone noon sun azimuth would be within
    // 1 degree of +-180, given that the sun only moves 0.25 degree per minute and SunCalc says
    // it's accurate to ~1 minute. Yet this example is off by nearly 5 degrees. Noon time (and
    // azimuth) vary by around +- 5 deg (or +- 20 min) based on when the on() time is.
    //
    // Based on cross-checking with online calculators, I believe that SunPosition is pretty
    // accurate, and SunTimes is doing something wrong with its interpolation or curve-fitting.

    assertNotEquals(180.0, sunPosition.getAzimuth(), 0.25); // SHOULD BE EQUAL WITHIN DELTA
  }

  @Test
  public void testAzimunthCloseNadir_bad() {
    SunTimes sunTimes = SunTimes.compute().at(COORDS).on(NOON_NADIR_BAD_TIME).execute();
    Date nadir = sunTimes.getNadir();
    SunPosition sunPosition = SunPosition.compute().at(COORDS).on(nadir).execute();

    // It seems reasonable to expect that northern temperate zone noon sun azimuth would be within
    // 1 degree of +-180, given that the sun only moves 0.25 degree per minute and SunCalc says
    // it's accurate to ~1 minute. Yet this example is off by nearly 5 degrees. Noon time (and
    // azimuth) vary by around +- 5 deg (or +- 20 min) based on when the on() time is.
    //
    // Based on cross-checking with online calculators, I believe that SunPosition is pretty
    // accurate, and SunTimes is doing something wrong with its interpolation or curve-fitting.

    assertNotEquals(
        0.0, toZeroCentered(sunPosition.getAzimuth()), 0.25); // SHOULD BE EQUAL WITHIN DELTA
  }

  @Test
  public void testSunInfoCorrectorNoon() {
    SunTimes sunTimes = SunTimes.compute().at(COORDS).on(NOON_NADIR_BAD_TIME).execute();
    Date noon = sunTimes.getNoon();

    Date correctedNoon = NoonNadirCorrector.correct(noon, COORDS);
    SunPosition sunPosition = SunPosition.compute().at(COORDS).on(correctedNoon).execute();

    assertEquals(180.0, sunPosition.getAzimuth(), 0.25);
  }

  @Test
  public void testSunInfoCorrectorNadir() {
    SunTimes sunTimes = SunTimes.compute().at(COORDS).on(NOON_NADIR_BAD_TIME).execute();
    Date nadir = sunTimes.getNadir();

    Date correctedNadir = NoonNadirCorrector.correct(nadir, COORDS);
    SunPosition sunPosition = SunPosition.compute().at(COORDS).on(correctedNadir).execute();

    assertEquals(0.0, toZeroCentered(sunPosition.getAzimuth()), 0.25);
  }

  /** Converts an angle in the range [0,360) to one in the range (-180,180]. */
  private static double toZeroCentered(double angle) {
    return angle > 180.0 ? angle - 360.0 : angle;
  }
}
