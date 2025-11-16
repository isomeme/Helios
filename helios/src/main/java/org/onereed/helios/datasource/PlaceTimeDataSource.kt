package org.onereed.helios.datasource

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.onereed.helios.common.PlaceTime
import timber.log.Timber

@OptIn(ExperimentalTime::class)
class PlaceTimeDataSource(private val activity: AppCompatActivity) {

  private val locationProvider: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(activity)

  private val _placeTimeFlow = MutableSharedFlow<PlaceTime>(replay = 1)

  val placeTimeFlow = _placeTimeFlow.asSharedFlow()

  init {
    activity.lifecycleScope.launch {
      activity.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        getLocationUpdates().collect { _placeTimeFlow.emit(PlaceTime(it, now())) }
      }
    }
  }

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
    if (activity.checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
      Timber.d("Not requesting location updates; permission not granted yet.")
      close()
      return@callbackFlow
    }

    val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          locationResult.lastLocation?.let {
            trySend(it).onFailure { t -> Timber.e(t, "Failed to send location.") }
          }
        }
      }

    locationProvider
      .requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper())
      .addOnSuccessListener { Timber.d("Location updates started.") }
      .addOnFailureListener { e ->
        Timber.e(e, "Location updates start failed.")
        close(e)
      }

    awaitClose {
      locationProvider
        .removeLocationUpdates(locationCallback)
        .addOnSuccessListener { Timber.d("Location updates stopped.") }
        .addOnFailureListener { e -> Timber.e(e, "Location updates stop failed.") }
    }
  }

  companion object {

    private val UPDATE_INTERVAL = 30.seconds.inWholeMilliseconds
    private val MIN_UPDATE_INTERVAL = 15.seconds.inWholeMilliseconds

    private val LOCATION_REQUEST =
      LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()
  }
}
