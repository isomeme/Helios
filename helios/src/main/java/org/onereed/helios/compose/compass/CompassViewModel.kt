package org.onereed.helios.compose.compass

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.onereed.helios.common.Orienter
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import org.onereed.helios.common.Ticker

@Suppress("unused")
@HiltViewModel
class CompassViewModel @Inject constructor(orienter: Orienter) : ViewModel() {

  val tickerFlow = Ticker(10.milliseconds).flow

  val headingFlow = orienter.flow
}
