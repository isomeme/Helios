package org.onereed.helios.compose

import org.onereed.helios.common.PlaceTime

data class ScheduleUi(val placeTimeStr: String, val buttons: List<EventUi>) {
  data class EventUi(val name: String, val ordinal: Int)

  companion object {

    fun create(placeTime: PlaceTime, sunResources: SunResources): ScheduleUi {
      val placeTimeStr = placeTime.toString()

      val buttons =
        sunResources.eventSets.mapIndexed { ix, eventSet ->
          EventUi(name = eventSet.name, ordinal = ix)
        }

      return ScheduleUi(placeTimeStr, buttons)
    }
  }
}
