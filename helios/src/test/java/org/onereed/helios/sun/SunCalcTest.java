package org.onereed.helios.sun;

import org.junit.Test;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/** Tests demonstrating bugs or feature requests in SunCalc. */
public class SunCalcTest {

  @Test
  public void bugDemo_missedSunset() {
    // At subarctic latitudes, it seems reasonable that SunCalc in non-fullCycle mode will find a
    // sunrise and sunset in the day ahead from any start time. However, when these events are daily
    // happening later (e.g. sunset in winter and spring), calculation from just after the previous
    // sunset // will miss the next one.

    // The effect is larger the further you are from the equator.
    double[] coords = {70.0, 0.0};

    // The effect is largest near the equinox.
    Instant when = Instant.parse("2020-03-20T00:00:00Z");

    ZonedDateTime firstSunset =
        checkNotNull(SunTimes.compute().at(coords).on(when).execute().getSet());
    ZonedDateTime justAfterFirstSunset = firstSunset.plusSeconds(30L);
    ZonedDateTime secondSunset =
        SunTimes.compute().at(coords).on(justAfterFirstSunset).execute().getSet();

    // We stop looking after exactly 1 day, so we missed the next sunset.
    assertNull(secondSunset);

    ZonedDateTime secondSunsetFullCycle =
        SunTimes.compute().at(coords).on(justAfterFirstSunset).fullCycle().execute().getSet();

    // FullCycle finds it just a little later on.
    assertNotNull(secondSunsetFullCycle);
    assertEquals(
        Duration.ofSeconds(205L),
        Duration.between(justAfterFirstSunset.plusDays(1), secondSunsetFullCycle));
  }
}
