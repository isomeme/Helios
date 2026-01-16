package org.onereed.helios.compose.compass

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import javax.inject.Inject
import org.onereed.helios.R
import org.onereed.helios.common.Point
import org.onereed.helios.compose.compass.ZIndex.SUN_AND_ARROW
import org.onereed.helios.compose.compass.ZIndex.SUN_EVENT
import org.onereed.helios.compose.shared.SUN_ORDINAL
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.sun.SunCompass

@Immutable
data class CompassUi(val items: List<CompassItem>, val isValid: Boolean) {

  @Immutable
  data class CompassItem(
    @DrawableRes val iconRes: Int,
    @StringRes val nameRes: Int,
    val ordinal: Int,
    val zIndex: Float,
    val point: Point,
    val scale: Float,
    val rotation: Float,
  ) {
    companion object {

      /**
       * @param ordinal The sun event ordinal 0-3, or 4 for the sun and sun arrow.
       * @param zIndexEnum The items's placement in the display stack.
       * @param angle Angle in degrees clockwise from north.
       * @param relativeRadius Radius as a fraction of the compass radius.
       * @param relativeScale Scale as a fraction of the default scale.
       * @param rotation Rotation in degrees of the item itself around its center.
       */
      fun create(
        @DrawableRes iconRes: Int,
        @StringRes nameRes: Int,
        ordinal: Int,
        zIndexEnum: ZIndex,
        angle: Float,
        relativeRadius: Float = 1f,
        relativeScale: Float = 1f,
        rotation: Float = 0f,
      ): CompassItem {
        val point = Point.fromPolar(angle, RADIAL_SCALE * relativeRadius)
        val scale = relativeScale * ITEM_SCALE

        return CompassItem(iconRes, nameRes, ordinal, zIndexEnum.zIndex, point, scale, rotation)
      }

      /** The fraction of the graphics layer size corresponding to the compass circle radius. */
      private const val RADIAL_SCALE = 0.44f

      /** The scale of positionally rendered images as a fraction of the graphics layer size. */
      private const val ITEM_SCALE = 0.1f
    }
  }

  class Factory @Inject constructor(val sunResources: SunResources) {

    fun create(sunCompass: SunCompass): CompassUi {
      if (!sunCompass.isValid) return CompassUi(items = emptyList(), isValid = false)

      val sunAngle = sunCompass.sunAzimuth.toFloat()
      val arrowRotation = if (sunCompass.isSunClockwise) sunAngle else sunAngle + 180f
      
      val sunItem =
        CompassItem.create(
          iconRes = R.drawable.ic_solid_dot,
          nameRes = R.string.content_sun_position,
          ordinal = SUN_ORDINAL,
          zIndexEnum = SUN_AND_ARROW,
          angle = sunAngle,
        )

      val arrowItem =
        CompassItem.create(
          iconRes = R.drawable.ic_baseline_arrow_forward_24,
          nameRes = R.string.content_sun_movement_direction,
          ordinal = SUN_ORDINAL,
          zIndexEnum = SUN_AND_ARROW,
          angle = sunAngle,
          relativeRadius = 0.6f,
          relativeScale = 0.7f,
          rotation = arrowRotation,
        )

      val eventItems =
        sunCompass.events.map { (eventType, event) ->
          val ordinal = eventType.ordinal
          val eventSet = sunResources.eventSets[ordinal]
          val angle = event.azimuth.toFloat()
          val radius = if (eventType == sunCompass.noonNadirOverlap) 0.8f else 1f

          CompassItem.create(
            iconRes = R.drawable.ic_sol_symbol,
            nameRes = eventSet.nameRes,
            ordinal = ordinal,
            zIndexEnum = SUN_EVENT,
            angle = angle,
            relativeRadius = radius,
          )
        }

      val allItems = eventItems + sunItem + arrowItem

      return CompassUi(items = allItems, isValid = true)
    }
  }
}
