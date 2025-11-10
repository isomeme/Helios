package org.onereed.helios.compose

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class TextUi(val menu: List<EventUi>, val selected: EventUi, val rubric: String) {
  data class EventUi(
    @param:DrawableRes val iconRes: Int,
    val color: Color,
    val name: String,
    val enabled: Boolean,
    val index: Int,
  )

  companion object {

    fun create(
      sunResources: SunResources,
      selectedIndex: Int,
    ): TextUi {
      val menu =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(
            iconRes = eventSet.iconRes,
            color = eventSet.fgColor,
            name = eventSet.name,
            enabled = ix != selectedIndex,
            index = ix,
          )
        }

      val selected = menu[selectedIndex]
      val rubric = sunResources.eventSets[selectedIndex].rubric

      return TextUi(menu, selected, rubric)
    }
  }
}
