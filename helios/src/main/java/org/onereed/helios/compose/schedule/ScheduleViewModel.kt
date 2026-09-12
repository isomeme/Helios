package org.onereed.helios.compose.schedule

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.compose.text.SelectTextIndexUseCase
import org.onereed.helios.datasource.Locator
import org.onereed.shared.ui.UiState
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel
@Inject
constructor(
  locator: Locator,
  uiFactory: ScheduleUi.Factory,
  val selectTextIndex: SelectTextIndexUseCase,
) : BaseViewModel() {

  val uiStateFlow =
    locator
      .placeTimeFlow()
      .map { uiFactory.create(it) }
      .map { UiState.Success(it) }
      .stateIn(initialValue = UiState.Loading)
}
