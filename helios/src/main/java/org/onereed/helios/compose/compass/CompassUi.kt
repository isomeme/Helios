package org.onereed.helios.compose.compass

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.lang.Math.toRadians
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import org.onereed.helios.R
import org.onereed.helios.sun.SunCompass

data class CompassUi(val items: List<CompassItem>) {
  data class CompassItem(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val nameRes: Int,
    val ordinal: Int,
    val point: Point,
    val scale: Float = ITEM_SCALE,
    val rotation: Float = 0f,
  )

  /** Cartesian coordinates as fractions of the graphics layer size. */
  data class Point(val x: Float, val y: Float) {
    companion object {
      /**
       * @param angle Angle in degrees clockwise from north.
       * @param radius Radius as a fraction of the compass circle radius.
       * @return Equivalent Cartesian coordinates as fractions of the graphics layer size.
       */
      fun fromPolar(angle: Float, radius: Float = 1f): Point {
        val radAngle = toRadians(angle.toDouble()).toFloat()
        val scaledRadius = RADIAL_SCALE * radius

        return Point(x = sin(radAngle) * scaledRadius, y = -cos(radAngle) * scaledRadius)
      }

      /**
       * The fraction of the graphics layer size corresponding to the radius of the compass circle.
       */
      private const val RADIAL_SCALE = 0.44f
    }
  }

  class Factory @Inject constructor() {
    fun create(sunCompass: SunCompass): CompassUi {
      val sunAngle = sunCompass.sunAzimuth.toFloat()

      val sunItem =
        CompassItem(
          iconRes = R.drawable.ic_solid_dot,
          nameRes = R.string.content_sun_position,
          ordinal = 4,
          point = Point.fromPolar(angle = sunAngle),
        )

      val arrowItem =
        CompassItem(
          iconRes = R.drawable.ic_baseline_arrow_forward_24,
          nameRes = R.string.content_sun_movement_direction,
          ordinal = 4,
          point = Point.fromPolar(angle = sunAngle, radius = 0.6f),
          scale = 0.7f * ITEM_SCALE,
          rotation = if (sunCompass.isSunClockwise) sunAngle else sunAngle + 180f,
        )

      return CompassUi(listOf(sunItem, arrowItem))
    }
  }

  companion object {

    /** The scale of positionally rendered images as a fraction of the graphics layer size. */
    private const val ITEM_SCALE = 0.1f
  }
}
