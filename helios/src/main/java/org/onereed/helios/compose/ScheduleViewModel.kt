package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(sunResources: SunResources) : ViewModel() {

  val ui = ScheduleUi.create(sunResources)
}
