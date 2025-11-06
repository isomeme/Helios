package org.onereed.helios.compose

import androidx.compose.ui.graphics.Color

data class TextUi(val menu: List<EventUi>, val selected: EventUi, val rubric: String) {
  data class EventUi(
    val icon: Int,
    val color: Color,
    val name: String,

    // The last two fields are only meaningful in the menu, so we provide default values for
    // convenience in populating TextState.selected in tests and previews.

    val enabled: Boolean = true,
    val onSelect: () -> Unit = {},
  )

  companion object {

    fun create(
      sunResources: SunResources,
      selectedIndex: Int,
      selectionConsumer: (Int) -> Unit,
    ): TextUi {
      val menu =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(
            icon = eventSet.icon,
            color = eventSet.fgColor,
            name = eventSet.name,
            enabled = ix != selectedIndex,
            onSelect = { selectionConsumer(ix) },
          )
        }

      val selected = menu[selectedIndex]
      val rubric = sunResources.eventSets[selectedIndex].rubric

      return TextUi(menu, selected, rubric)
    }
  }
}
