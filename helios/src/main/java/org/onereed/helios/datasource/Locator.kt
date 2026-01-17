package org.onereed.helios.datasource

import kotlinx.coroutines.flow.Flow

interface Locator {

  fun placeTimeFlow(): Flow<PlaceTime>
}
