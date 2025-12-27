package org.onereed.helios.compose.text

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import javax.inject.Inject
import org.onereed.helios.datasource.SunResources

@Immutable
data class TextUi(val menu: List<EventUi>, val selected: EventUi, val rubric: String) {
  @Immutable
  data class EventUi(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val nameRes: Int,
    val enabled: Boolean,
    val index: Int,
  )

  class Factory @Inject constructor(private val sunResources: SunResources) {

    fun create(selectedIndex: Int): TextUi {
      val menu =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(
            iconRes = eventSet.iconRes,
            nameRes = eventSet.nameRes,
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
