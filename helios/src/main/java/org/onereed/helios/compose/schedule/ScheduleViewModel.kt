package org.onereed.helios.compose.schedule

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel
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
) : BaseViewModel() {

  val scheduleUiFlow =
    locator.flow.mapState(::SunTimeSeries).mapState(::SunSchedule).mapState(uiFactory::create)
}
