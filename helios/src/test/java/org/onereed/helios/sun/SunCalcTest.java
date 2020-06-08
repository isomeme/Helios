package org.onereed.helios.sun;

import org.junit.Test;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** Tests demonstrating bugs in SunCalc */
public class SunCalcTest {

  @Test
  public void noonFarFrom180() {
    double[] where = {34.0, -118.5};
    Instant when = Instant.parse("2020-06-08T04:17:00Z");
    SunTimes sunTimes = SunTimes.compute().at(where).on(when).execute();
    ZonedDateTime noon = sunTimes.getNoon();
    assertNotNull(noon);

    SunPosition sunPosition = SunPosition.compute().at(where).on(noon).execute();
    double noonAzimuth = sunPosition.getAzimuth();

    assertEquals(180.0, noonAzimuth, 0.25); // Fails, noonAzimuth = 180.96613610900388
  }
}
