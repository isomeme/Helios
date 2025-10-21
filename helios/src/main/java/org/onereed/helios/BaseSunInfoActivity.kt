package org.onereed.helios

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.onereed.helios.sun.SunInfo
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.Executor

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

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        getLocationUpdates().collect { sunInfoViewModel.acceptLocation(it) }
      }
    }
  }

  fun getLocationUpdates(): Flow<Location> = callbackFlow {
    Timber.d("getLocationUpdates start")

    if (checkLocationPermission()) {
      val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
              trySend(location).onFailure { t -> Timber.e(t, "Failed to send location.") }
            }
          }
        }

      locationProvider
        .requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper())
        .addOnSuccessListener { Timber.d("Location updates started.") }
        .addOnFailureListener { e ->
          Timber.e(e, "Location updates start failed.")
          close(e) // Close the flow with an error if the request fails
        }

      awaitClose {
        // Clean up resources when the flow is no longer collected
        locationProvider
          .removeLocationUpdates(locationCallback)
          .addOnSuccessListener { Timber.d("Location updates stopped.") }
          .addOnFailureListener { e -> Timber.e(e, "Location updates stop failed.") }
      }
    } else {
      Timber.d("Not requesting location updates; permission not granted yet.")
      close(SecurityException("Location permission not granted"))
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

  protected fun checkLocationPermission(): Boolean =
    checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

  private fun requestLocationPermission() = requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)

  companion object {

    private val UPDATE_INTERVAL = Duration.ofSeconds(30L).toMillis()
    private val MIN_UPDATE_INTERVAL = Duration.ofSeconds(15L).toMillis()

    private val LOCATION_REQUEST =
      LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()
  }
}
