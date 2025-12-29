package org.onereed.helios.datasource

import kotlinx.coroutines.flow.StateFlow

interface Locator {

  fun placeTimeFlow(): StateFlow<PlaceTime>
}
