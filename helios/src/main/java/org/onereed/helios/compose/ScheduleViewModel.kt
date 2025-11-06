package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel
@Inject
constructor(
  private val textStateHolder: TextStateHolder,
  private val navigator: Navigator,
  sunResources: SunResources,
) : ViewModel() {

  val ui = ScheduleUi.create(sunResources) { navigateToText(it) }

  fun navigateToText(sunEventIndex: Int) {
    textStateHolder.selectIndex(sunEventIndex)
    navigator.navigateTo(AppDestination.TEXT)
  }
}
