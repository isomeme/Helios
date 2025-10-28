package org.onereed.helios

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.onereed.helios.databinding.ActivityScheduleBinding
import org.onereed.helios.datasource.PlaceTimeDataSource

/** Displays the schedule of sun events. */
class ScheduleActivity : BaseActivity() {

  private lateinit var placeTimeDataSource: PlaceTimeDataSource

  private val sunScheduleViewModel: SunScheduleViewModel by viewModels()

  @IdRes override val myActionsMenuId = R.id.action_schedule

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityScheduleBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val sunScheduleAdapter = SunScheduleAdapter(this)

    binding.sunEventsRecyclerView.apply {
      layoutManager = LinearLayoutManager(this@ScheduleActivity)
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
