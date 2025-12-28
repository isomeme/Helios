package org.onereed.helios.compose.compass

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
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
  compassUiFactory: CompassUi.Factory,
  private val storeRepository: StoreRepository,
) : BaseViewModel() {

  val compassUiFlow =
    locator.placeTimeFlow
      .mapState(::SunTimeSeries)
      .mapState(SunCompass::compute)
      .mapState(compassUiFactory::create)

  val isLockedFlow = storeRepository.isCompassLockedFlow.stateIn(initialValue = false)

  private val lockedHeadingFlow =
    storeRepository.isCompassSouthTopFlow
      .map { southTop -> if (southTop) 180f else 0f }
      .stateIn(initialValue = 0f)

  @OptIn(ExperimentalCoroutinesApi::class)
  val headingFlow =
    isLockedFlow
      .flatMapLatest { locked ->
        if (locked) {
          lockedHeadingFlow
        } else {
          orienter.headingFlow(initial = lockedHeadingFlow.value)
        }
      }
      .stateIn(initialValue = 0f)

  fun setLocked(locked: Boolean) {
    storeRepository.setCompassLocked(value = locked, scope = viewModelScope)
  }
}
