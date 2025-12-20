package org.onereed.helios.compose.compass

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import org.onereed.helios.common.Ticker

@HiltViewModel
class CompassViewModel @Inject constructor() : ViewModel() {

  val tickerFlow = Ticker(3.seconds).flow
}
