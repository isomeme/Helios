package org.onereed.helios.datasource

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.onereed.helios.R
import org.onereed.helios.common.Place
import timber.log.Timber
import java.time.Duration

class PlaceDataSource(private val activity: AppCompatActivity) {

  private val locationProvider: FusedLocationProviderClient

  private val requestPermissionLauncher: ActivityResultLauncher<String>

  private val _placeFlow = MutableSharedFlow<Place>(replay = 1)

  val placeFlow = _placeFlow.asSharedFlow()

  init {
    Timber.Forest.d("init start")

    locationProvider = LocationServices.getFusedLocationProviderClient(activity)

    requestPermissionLauncher =
      activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        acceptLocationPermissionResult(isGranted)
      }

    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
      override fun onStart(owner: LifecycleOwner) {
        maybeRequestLocationPermission()
      }
    })

    activity.lifecycleScope.launch {
      activity.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        getLocationUpdates().collect {
          Timber.Forest.d("Location update: $it")
          _placeFlow.emit(Place(it))
        }
      }
    }
  }

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
      Timber.Forest.d("getLocationUpdates start")

      if (!checkLocationPermission()) {
          Timber.Forest.d("Not requesting location updates; permission not granted yet.")
          close()
          return@callbackFlow
      }

      val locationCallback =
          object : LocationCallback() {
              override fun onLocationResult(locationResult: LocationResult) {
                  locationResult.lastLocation?.let { location ->
                      trySend(location).onFailure { t ->
                          Timber.Forest.e(
                              t,
                              "Failed to send location."
                          )
                      }
                  }
              }
          }

      locationProvider
          .requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper())
          .addOnSuccessListener { Timber.Forest.d("Location updates started.") }
          .addOnFailureListener { e ->
              Timber.Forest.e(e, "Location updates start failed.")
              close(e)
          }

      awaitClose {
          locationProvider
              .removeLocationUpdates(locationCallback)
              .addOnSuccessListener { Timber.Forest.d("Location updates stopped.") }
              .addOnFailureListener { e -> Timber.Forest.e(e, "Location updates stop failed.") }
      }
  }

  private fun maybeRequestLocationPermission() {
    Timber.Forest.d("maybeRequestLocationPermission start")

    if (checkLocationPermission()) {
      Timber.Forest.d("Location permission already granted.")
    } else {
      Timber.Forest.d("Requesting location permission.")
      requestLocationPermission()
    }
  }

  private fun checkLocationPermission(): Boolean =
    activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

  private fun requestLocationPermission() = requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

  private fun acceptLocationPermissionResult(isGranted: Boolean) {
    Timber.Forest.d("acceptLocationPermissionResult: isGranted=$isGranted")

    if (isGranted) {
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      activity.window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
      AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_rationale)
        .setPositiveButton(R.string.button_continue) { _, _ -> requestLocationPermission() }
        .setNegativeButton(R.string.button_exit) { _, _ -> activity.finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.Forest.d("Launched rationale dialog.")
    } else {
      val settingsIntent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          .setData(Uri.fromParts("package", activity.packageName, /* fragment= */ null))

      AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_use_settings)
        .setPositiveButton(R.string.button_settings) { _, _ -> activity.startActivity(settingsIntent) }
        .setNegativeButton(R.string.button_exit) { _, _ -> activity.finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.Forest.d("Launched use-settings dialog.")
    }
  }

  companion object {

    private val UPDATE_INTERVAL = Duration.ofSeconds(30L).toMillis()
    private val MIN_UPDATE_INTERVAL = Duration.ofSeconds(15L).toMillis()

    private val LOCATION_REQUEST =
      LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()
  }
}
