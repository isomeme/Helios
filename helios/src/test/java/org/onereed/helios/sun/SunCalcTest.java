package org.onereed.helios.sun;

import org.junit.Test;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * A test showing that {@link SunTimes} returns null noon and nadir values for calculations done at
 * {@link Date} values near these times. Tests all pass; those which demonstrate the unexpected
 * null-return behavior have the name suffix "_bad". Note that {@link
 * #testJustBeforeNoonFullCycle_bad()} shows that the use of full-cycle mode does not change the
 * unexpected null-return behavior.
 */
public class SunCalcTest {

  // All time and location parameters are for local solar noon in Santa Monica CA USA on
  // 2020-05-03 12:52:00 GMT-07:00

  private static final Instant NOON = Instant.parse("2020-05-03T19:51:00Z");
  private static final double[] COORDS = {34.0, -118.5};

  private static final Duration SHORT_DURATION = Duration.ofMinutes(2L);
  private static final Duration LONG_DURATION = Duration.ofMinutes(30L);
  private static final Duration ONE_DAY = Duration.ofDays(1L);

  @Test
  public void testWellBeforeNoon() {
    Date wellBeforeNoon = Date.from(NOON.minus(LONG_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(wellBeforeNoon).at(COORDS).execute();

    assertEquals(NOON, sunTimes.getNoon().toInstant());
  }

  @Test
  public void testJustBeforeNoon_bad() {
    Date justBeforeNoon = Date.from(NOON.minus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justBeforeNoon).at(COORDS).execute();

    assertNull(sunTimes.getNoon()); // SHOULD NOT BE NULL
  }

  @Test
  public void testJustAfterNoon_bad() {
    Date justAfterNoon = Date.from(NOON.plus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justAfterNoon).at(COORDS).execute();

    assertNull(sunTimes.getNoon()); // SHOULD NOT BE NULL
  }

  @Test
  public void testWellAfterNoon() {
    Date wellAfterNoon = Date.from(NOON.plus(LONG_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(wellAfterNoon).at(COORDS).execute();

    // It happens that May 4 noon is at the exact same (minute-rounded) time as May 3.
    assertEquals(NOON.plus(ONE_DAY), sunTimes.getNoon().toInstant());
  }

  @Test
  public void testJustBeforeNadir_bad() {
    Date wellAfterNoon = Date.from(NOON.plus(LONG_DURATION));
    SunTimes sunTimes1 = SunTimes.compute().on(wellAfterNoon).at(COORDS).execute();
    Instant nadir = sunTimes1.getNadir().toInstant();
    Date justBeforeNadir = Date.from(nadir.minus(SHORT_DURATION));
    SunTimes sunTimes2 = SunTimes.compute().on(justBeforeNadir).at(COORDS).execute();

    assertNull(sunTimes2.getNadir()); // SHOULD NOT BE NULL
  }

  @Test
  public void testJustBeforeNoonFullCycle_bad() {
    Date justBeforeNoon = Date.from(NOON.minus(SHORT_DURATION));
    SunTimes sunTimes = SunTimes.compute().on(justBeforeNoon).fullCycle().at(COORDS).execute();

    assertNull(sunTimes.getNoon()); // SHOULD NOT BE NULL
  }
}
