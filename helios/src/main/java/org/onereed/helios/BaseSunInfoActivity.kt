package org.onereed.helios

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.onereed.helios.datasource.PlaceTimeDataSource
import org.onereed.helios.sun.SunInfo
import timber.log.Timber

abstract class BaseSunInfoActivity : BaseActivity() {

  private lateinit var placeTimeDataSource: PlaceTimeDataSource

  private val sunInfoViewModel: SunInfoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate")
    super.onCreate(savedInstanceState)

    placeTimeDataSource = PlaceTimeDataSource(this)

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        placeTimeDataSource.placeTimeFlow.collect { sunInfoViewModel.acceptPlace(it) }
      }
    }
  }

  protected fun observeSunInfo(sunInfoFlowCollector: FlowCollector<SunInfo>) {
    sunInfoViewModel.viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        sunInfoViewModel.sunInfoFlow.collect(sunInfoFlowCollector)
      }
    }
  }
}
