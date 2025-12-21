package org.onereed.helios.common

import kotlin.math.IEEErem
import kotlin.math.abs

/** Returns the signed angular distance from [from] to [to] in the range `[-180..180]`. */
fun arc(from: Double, to: Double): Double = (to - from).IEEErem(360.0)

fun arc(from: Float, to: Float): Float = arc(from.toDouble(), to.toDouble()).toFloat()

/**
 * Returns the absolute value of the angular distance from [from] to [to] in the range `[0..180]`.
 */
fun ang(from: Double, to: Double) = abs(arc(from, to))
