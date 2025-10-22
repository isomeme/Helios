package org.onereed.helios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.onereed.helios.common.Place
import org.onereed.helios.sun.SunInfo
import timber.log.Timber
import java.time.Instant

/** Stores and updates data needed for [SunInfo] display. */
class SunInfoViewModel : ViewModel() {

  private val _sunInfoFlow = MutableSharedFlow<SunInfo>(replay = 1)

  val sunInfoFlow = _sunInfoFlow.asSharedFlow()

  fun acceptPlace(place: Place) {
    Timber.d("acceptPlace start: $place")

    viewModelScope.launch(Dispatchers.Default) {
      val sunInfo = SunInfo.compute(place, Instant.now())
      _sunInfoFlow.emit(sunInfo)
    }
  }
}
