package org.onereed.helios.common

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@Singleton
class Locator @Inject constructor(@param:ApplicationContext val context: Context) {

  private val locationProvider: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(context)

  val flow =
    getLocationUpdates()
      .map { PlaceTime(it, Instant.now()) }
      .onStart { Timber.d("Locator.onStart") }
      .onEach { Timber.d("Locator.onEach $it") }
      .onCompletion { Timber.d("Locator.onCompletion") }

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
    if (context.checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
      Timber.d("Not requesting location updates; permission not granted yet.")
      close()
      return@callbackFlow
    }

    val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          Timber.d("Location result received: ${locationResult.lastLocation}")
          locationResult.lastLocation?.let {
            trySend(it)
              .onSuccess { Timber.d("Location sent to flow.") }
              .onFailure { t -> Timber.e(t, "Failed to send location to flow.") }
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

    private val UPDATE_INTERVAL = Duration.ofSeconds(30L).toMillis()
    private val MIN_UPDATE_INTERVAL = Duration.ofSeconds(15L).toMillis()

    private val LOCATION_REQUEST =
      LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()
  }
}
