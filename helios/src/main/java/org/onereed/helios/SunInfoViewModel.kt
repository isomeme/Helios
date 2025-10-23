package org.onereed.helios

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.sun.SunInfo
import org.onereed.helios.sun.SunTimeSeries
import timber.log.Timber

/** Stores and updates data needed for [SunInfo] display. */
class SunInfoViewModel : ViewModel() {

  private val _sunInfoFlow = MutableSharedFlow<SunInfo>(replay = 1)

  val sunInfoFlow = _sunInfoFlow.asSharedFlow()

  suspend fun acceptPlace(place: PlaceTime) {
    Timber.d("acceptPlace start: $place")

//    viewModelScope.launch(Dispatchers.Default) {
      val sunTimeSeries = SunTimeSeries.compute(place)
      val sunInfo = SunInfo.compute(sunTimeSeries)
      _sunInfoFlow.emit(sunInfo)
//    }
  }
}
