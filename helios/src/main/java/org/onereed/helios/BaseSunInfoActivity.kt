package org.onereed.helios

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.Executor
import org.onereed.helios.sun.SunInfo
import timber.log.Timber

abstract class BaseSunInfoActivity : BaseActivity() {

  private val sunInfoViewModel: SunInfoViewModel by viewModels()

  private lateinit var locationProvider: FusedLocationProviderClient
  private lateinit var mainExecutor: Executor

  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate")
    super.onCreate(savedInstanceState)

    locationProvider = LocationServices.getFusedLocationProviderClient(this)
    mainExecutor = ContextCompat.getMainExecutor(this)

    requestPermissionLauncher =
      registerForActivityResult(RequestPermission()) { isGranted ->
        acceptLocationPermissionResult(isGranted)
      }
  }

  override fun onStart() {
    Timber.d("onStart")
    super.onStart()

    if (checkLocationPermission()) {
      Timber.d("Location permission already granted")
    } else {
      Timber.d("Requesting location permission")
      requestLocationPermission()
    }
  }

  override fun onResume() {
    Timber.d("onResume")
    super.onResume()

    if (checkLocationPermission()) {
      Timber.d("About to request location updates.")

      locationProvider
        .requestLocationUpdates(LOCATION_REQUEST, mainExecutor, sunInfoViewModel)
        .addOnSuccessListener { Timber.d("Location updates started.") }
        .addOnFailureListener { e -> Timber.e(e, "Location updates start failed.") }

      // TODO: Add failure indicator to UI.
    } else {
      Timber.d("Not requesting location updates; permission not granted yet.")
    }
  }

  override fun onPause() {
    Timber.d("onPause")
    super.onPause()

    locationProvider
      .removeLocationUpdates(sunInfoViewModel)
      .addOnSuccessListener { Timber.d("Location updates stopped.") }
      .addOnFailureListener { e -> Timber.e(e, "Location updates stop failed.") }
  }

  protected fun observeSunInfo(sunInfoFlowCollector: FlowCollector<SunInfo?>) {
    lifecycleScope.launch {
      sunInfoViewModel.sunInfo.collect(sunInfoFlowCollector)
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

  protected fun checkLocationPermission(): Boolean {
    return checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
  }

  private fun requestLocationPermission() {
    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
  }

  companion object {

    private val UPDATE_INTERVAL = Duration.ofSeconds(30L).toMillis()
    private val MIN_UPDATE_INTERVAL = Duration.ofSeconds(15L).toMillis()

    private val LOCATION_REQUEST =
      LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()
  }
}
