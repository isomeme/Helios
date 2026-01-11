package org.onereed.helios.common

import androidx.compose.runtime.Immutable
import kotlin.math.cos
import kotlin.math.sin

/** Cartesian coordinates for compass display in the the Compose graphics layer display space. */
@Immutable
data class Point(val x: Float, val y: Float) {
  companion object {

    /**
     * @param angle Angle in degrees clockwise from north (toward east).
     * @param radius Radius as a fraction of the graphics layer size.
     * @return Equivalent Cartesian coordinates as fractions of the graphics layer size.
     */
    fun fromPolar(angle: Float, radius: Float): Point {
      val radAngle = Math.toRadians(angle.toDouble()).toFloat()
      return Point(x = sin(radAngle) * radius, y = -cos(radAngle) * radius)
    }
  }
}
