package org.onereed.helios.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.onereed.helios.common.Locator
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import timber.log.Timber

@HiltViewModel
class ScheduleViewModel
@Inject
constructor(
  @ApplicationContext context: Context,
  sunResources: SunResources,
  locator: Locator,
  private val textStateHolder: TextStateHolder,
) : ViewModel() {

  val scheduleUiFlow =
    locator.flow
      .map(SunTimeSeries::compute)
      .map(SunSchedule::compute)
      .map { schedule -> ScheduleUi.create(context, schedule, sunResources) }
      .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), replay = 1)

  fun selectTextIndex(index: Int) {
    Timber.d("selectTextIndex: $index")
    textStateHolder.selectIndex(index)
  }
}
