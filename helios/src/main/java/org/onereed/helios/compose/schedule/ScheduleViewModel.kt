package org.onereed.helios.compose.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.onereed.helios.common.Locator
import org.onereed.helios.compose.text.SelectTextIndexUseCase
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries

@HiltViewModel
class ScheduleViewModel
@Inject
constructor(
  locator: Locator,
  uiFactory: ScheduleUi.Factory,
  val selectTextIndex: SelectTextIndexUseCase,
) : ViewModel() {

  val scheduleUiFlow =
    locator.flow
      .map(::SunTimeSeries)
      .map(::SunSchedule)
      .map(uiFactory::create)
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MILLIS),
        ScheduleUi(emptyList()),
      )

  companion object {

    private val FLOW_TIMEOUT_MILLIS = 5.seconds.inWholeMilliseconds
  }
}
