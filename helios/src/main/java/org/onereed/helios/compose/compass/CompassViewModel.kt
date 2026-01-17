package org.onereed.helios.compose.compass

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.datasource.Locator
import org.onereed.helios.datasource.Orienter
import org.onereed.helios.datasource.StoreRepository
import org.onereed.helios.sun.SunCompass
import org.onereed.helios.sun.SunTimeSeries

@HiltViewModel
class CompassViewModel
@Inject
constructor(
  orienter: Orienter,
  locator: Locator,
  compassUiFactory: CompassUi.Factory,
  private val storeRepository: StoreRepository,
) : BaseViewModel() {

  val compassUiFlow =
    locator
      .placeTimeFlow()
      .map(::SunTimeSeries)
      .map(SunCompass::compute)
      .map(compassUiFactory::create)

  val isLockedFlow = storeRepository.isCompassLockedFlow

  val headingFlow = orienter.headingFlow

  fun setLocked(locked: Boolean) {
    storeRepository.setCompassLocked(value = locked, scope = viewModelScope)
  }
}
