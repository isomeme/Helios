package org.onereed.helios.compose

data class ScheduleUi(val buttons: List<EventUi>) {
  data class EventUi(val name: String, val onSelect: () -> Unit = {})

  companion object {

    fun create(sunResources: SunResources, selectionConsumer: (Int) -> Unit): ScheduleUi {
      val buttons =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(name = eventSet.name, onSelect = { selectionConsumer(ix) })
        }

      return ScheduleUi(buttons)
    }
  }
}
