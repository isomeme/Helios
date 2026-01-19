package org.onereed.helios.datasource

import kotlinx.coroutines.flow.StateFlow

interface Locator {

  /**
   * [Locator] is bound as a singleton and provides a [StateFlow] so that the schedule and compass
   * screens can share its output.
   */
  fun placeTimeFlow(): StateFlow<PlaceTime>
}
