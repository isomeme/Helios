package org.onereed.helios

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.onereed.helios.databinding.ActivityMainBinding
import org.onereed.helios.datasource.PlaceTimeDataSource
import timber.log.Timber

/** Main activity for Helios. */
class MainActivity : BaseActivity() {

  private lateinit var placeTimeDataSource: PlaceTimeDataSource

  private val sunScheduleViewModel: SunScheduleViewModel by viewModels()

  @IdRes override val myActionsMenuId = R.id.action_schedule

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate")
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val sunScheduleAdapter = SunScheduleAdapter(this)

    binding.sunEventsRecyclerView.apply {
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = sunScheduleAdapter
    }

    placeTimeDataSource = PlaceTimeDataSource(this)

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        placeTimeDataSource.placeTimeFlow.collect { sunScheduleViewModel.acceptPlaceTime(it) }
      }
    }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        sunScheduleViewModel.sunScheduleFlow.collect { sunScheduleAdapter.acceptSunSchedule(it) }
      }
    }
  }
}
