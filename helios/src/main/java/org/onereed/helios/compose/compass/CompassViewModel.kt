package org.onereed.helios.compose.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.onereed.helios.common.Orienter
import org.onereed.helios.common.Ticker

@Suppress("unused")
@HiltViewModel
class CompassViewModel @Inject constructor(orienter: Orienter) : ViewModel() {

  val tickerFlow = Ticker(10.milliseconds).flow

  val headingFlow =
    orienter.flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MILLIS), 0f)

  companion object {

    private val FLOW_TIMEOUT_MILLIS = 5.seconds.inWholeMilliseconds
  }
}
