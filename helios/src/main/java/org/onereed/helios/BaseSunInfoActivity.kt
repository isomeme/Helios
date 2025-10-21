package org.onereed.helios

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.onereed.helios.sun.SunInfo
import timber.log.Timber

abstract class BaseSunInfoActivity : BaseActivity() {

  private lateinit var placeDataSource: PlaceDataSource
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

  private val sunInfoViewModel: SunInfoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate")
    super.onCreate(savedInstanceState)

    placeDataSource = PlaceDataSource(this)

    requestPermissionLauncher =
      registerForActivityResult(RequestPermission()) { isGranted ->
        acceptLocationPermissionResult(isGranted)
      }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        placeDataSource.place.collect { sunInfoViewModel.acceptPlace(it) }
      }
    }
  }

  protected fun observeSunInfo(sunInfoFlowCollector: FlowCollector<SunInfo?>) {
    sunInfoViewModel.viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        sunInfoViewModel.sunInfo.collect(sunInfoFlowCollector)
      }
    }
  }

  private fun acceptLocationPermissionResult(isGranted: Boolean) {
    Timber.d("acceptLocationPermissionResult: isGranted=$isGranted")

    if (isGranted) {
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
      AlertDialog.Builder(this)
        .setMessage(R.string.location_permission_rationale)
        .setPositiveButton(R.string.button_continue) { _, _ -> requestLocationPermission() }
        .setNegativeButton(R.string.button_exit) { _, _ -> finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.d("Launched rationale dialog.")
    } else {
      val settingsIntent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          .setData(Uri.fromParts("package", packageName, /* fragment= */ null))

      AlertDialog.Builder(this)
        .setMessage(R.string.location_permission_use_settings)
        .setPositiveButton(R.string.button_settings) { _, _ -> go(settingsIntent) }
        .setNegativeButton(R.string.button_exit) { _, _ -> finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.d("Launched use-settings dialog.")
    }
  }

  private fun requestLocationPermission() = requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
}
