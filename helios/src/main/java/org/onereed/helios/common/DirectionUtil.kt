package org.onereed.helios.common

import kotlin.math.IEEErem

/** Static utility methods for working with angular directions.  */
object DirectionUtil {

    /**
     * Returns the signed angular distance from [deg1] to [deg2] in the range `[-180..180]`.
     */
    @JvmStatic
    fun arc(deg1: Double, deg2: Double): Double {
        val diff = deg2 - deg1
        return diff.IEEErem(360.0)
    }
}
