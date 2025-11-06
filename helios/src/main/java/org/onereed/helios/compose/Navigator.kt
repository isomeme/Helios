package org.onereed.helios.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

  var currentDestination by mutableStateOf(AppDestination.SCHEDULE)
    private set

  fun navigateTo(destination: AppDestination) {
    currentDestination = destination
  }
}
