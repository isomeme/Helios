package org.onereed.helios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.sun.SunCompass
import org.onereed.helios.sun.SunTimeSeries
import timber.log.Timber

/** Stores and updates data needed for sun event schedule display. */
class SunCompassViewModel : ViewModel() {

  private val _sunCompassFlow = MutableSharedFlow<SunCompass>(replay = 1)

  val sunCompassFlow = _sunCompassFlow.asSharedFlow()

  fun acceptPlaceTime(placeTime: PlaceTime) {
    Timber.d("acceptPlaceTime start: $placeTime")

    viewModelScope.launch(Dispatchers.Default) {
      val sunTimeSeries = SunTimeSeries.compute(placeTime)
      val sunCompass = SunCompass.compute(sunTimeSeries)
      _sunCompassFlow.emit(sunCompass)
    }
  }
}
