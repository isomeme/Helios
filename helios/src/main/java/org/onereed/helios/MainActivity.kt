package org.onereed.helios

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import org.onereed.helios.databinding.ActivityMainBinding
import timber.log.Timber

/** Main activity for Helios. */
class MainActivity : BaseSunInfoActivity() {

  @IdRes
  override val myActionsMenuId = R.id.action_schedule

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate")
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val sunInfoAdapter = SunInfoAdapter(this)

    binding.sunEventsRecyclerView.apply {
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = sunInfoAdapter
    }

    observeSunInfo(sunInfoAdapter)
  }
}
