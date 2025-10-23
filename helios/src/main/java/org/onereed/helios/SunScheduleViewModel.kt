package org.onereed.helios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import timber.log.Timber

/** Stores and updates data needed for sun event schedule display. */
class SunScheduleViewModel : ViewModel() {

  private val _sunScheduleFlow = MutableSharedFlow<SunSchedule>(replay = 1)

  val sunScheduleFlow = _sunScheduleFlow.asSharedFlow()

  fun acceptPlaceTime(placeTime: PlaceTime) {
    Timber.d("acceptPlaceTime start: $placeTime")

    viewModelScope.launch(Dispatchers.Default) {
      val sunTimeSeries = SunTimeSeries.compute(placeTime)
      val sunSchedule = SunSchedule.compute(sunTimeSeries)
      _sunScheduleFlow.emit(sunSchedule)
    }
  }
}
