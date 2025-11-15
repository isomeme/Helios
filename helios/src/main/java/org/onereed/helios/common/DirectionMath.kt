package org.onereed.helios.common

import kotlin.math.IEEErem
import kotlin.math.abs

/** Returns the signed angular distance from [deg1] to [deg2] in the range `[-180..180]`. */
fun arc(deg1: Double, deg2: Double): Double = (deg2 - deg1).IEEErem(360.0)

/**
 * Returns the absolute value of the angular distance from [deg1] to [deg2] in the range `[0..180]`.
 */
fun ang(deg1: Double, deg2: Double) = abs(arc(deg1, deg2))
