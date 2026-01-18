package org.onereed.helios.compose.compass

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
  compassItemsFactory: CompassItems.Factory,
  private val storeRepository: StoreRepository,
) : BaseViewModel() {

  private val compassItemsFlow =
    locator
      .placeTimeFlow()
      .map(SunTimeSeries::create)
      .map(SunCompass::create)
      .map(compassItemsFactory::create)

  // Compass turns opposite heading.
  private val compassAngleFlow = orienter.headingFlow.map { heading -> 360f - heading }

  val compassUiFlow =
    combine(compassItemsFlow, compassAngleFlow, storeRepository.isCompassLockedFlow, ::CompassUi)
      .stateIn(initialValue = CompassUi.INITIAL)

  fun setLocked(locked: Boolean) =
    storeRepository.setCompassLocked(value = locked, scope = viewModelScope)
}
