package org.onereed.helios

import android.content.res.Resources
import androidx.compose.ui.graphics.Color

internal data class SunResources(
  val eventNames: List<String>,
  val fgColors: List<Color>,
  val bgColors: List<Color>,
  val icons: List<Int>
) {
  companion object {

    fun from(resources: Resources) : SunResources {
      val eventNames = resources.getStringArray(R.array.sun_event_names).toList()
      val fgColors = resources.getIntArray(R.array.sun_event_fg_colors).map(::Color)
      val bgColors = resources.getIntArray(R.array.sun_event_bg_colors).map(::Color)
      val icons =
        resources.obtainTypedArray(R.array.sun_event_icons).use { typedArray ->
          (0..3).map { ix -> typedArray.getResourceId(ix, 0) }
        }
      return SunResources(eventNames, fgColors, bgColors, icons)
    }
  }
}
