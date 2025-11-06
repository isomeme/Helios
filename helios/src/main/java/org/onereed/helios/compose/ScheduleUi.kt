package org.onereed.helios.compose

data class ScheduleUi(val buttons: List<EventUi>) {
  data class EventUi(val name: String, val ordinal: Int)

  companion object {

    fun create(sunResources: SunResources): ScheduleUi {
      val buttons =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(name = eventSet.name, ordinal = ix)
        }

      return ScheduleUi(buttons)
    }
  }
}
