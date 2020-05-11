package org.onereed.helios.sun;

import org.junit.Test;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

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

  /**
   * It happens that May 4 noon is at the exact same (minute-rounded) time as May 3.
   */
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
}