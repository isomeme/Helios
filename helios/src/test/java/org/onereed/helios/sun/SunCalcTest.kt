package org.onereed.helios.sun

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import java.time.Instant

/** Tests demonstrating bugs in SunCalc  */
class SunCalcTest {
    @Test
    fun noonNear180_good() {
        val where = doubleArrayOf(34.0, -118.5)
        val `when` = Instant.parse("2020-06-16T04:11:00Z")
        val sunTimes = SunTimes.compute().at(where).on(`when`).execute()
        val noon = sunTimes.getNoon()
        assertNotNull(noon)

        val sunPosition = SunPosition.compute().at(where).on(noon).execute()
        val noonAzimuth = sunPosition.getAzimuth()

        assertEquals(180.0, noonAzimuth, 0.25) // noonAzimuth = 180.00725554668216
    }
}
