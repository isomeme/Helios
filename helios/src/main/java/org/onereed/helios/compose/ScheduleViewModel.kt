package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import org.onereed.helios.common.Locator

@HiltViewModel
class ScheduleViewModel @Inject constructor(sunResources: SunResources, locator: Locator) :
  ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val scheduleUiFlow = locator.flow.mapLatest { ScheduleUi.create(it, sunResources) }
}
