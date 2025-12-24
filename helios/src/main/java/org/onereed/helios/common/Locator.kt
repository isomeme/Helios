package org.onereed.helios.common

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

@Singleton
@OptIn(ExperimentalTime::class)
class Locator @Inject constructor(
  @param:ApplicationContext private val context: Context,
  @param:ApplicationScope private val externalScope: CoroutineScope,
) {
  private val locationProvider by lazy { LocationServices.getFusedLocationProviderClient(context) }

  private val ticker = Ticker(TICKER_INTERVAL)

  val placeTimeFlow =
    getLocationUpdates()
      .combine(ticker.flow) { location, _ -> PlaceTime(location, now()) }
      .onStart { Timber.d("Locator.onStart") }
      .onEach { Timber.d("Locator.onEach $it") }
      .onCompletion { Timber.d("Locator.onCompletion") }
      .stateIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MILLIS),
        initialValue = PlaceTime.EMPTY,
      )

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
    if (context.checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
      Timber.d("Not requesting location updates; permission not granted yet.")
      close()
      return@callbackFlow
    }

    // Grab the last location to send something down the flow before we start listening for updates.

    locationProvider.lastLocation
      .addOnSuccessListener { location ->
        Timber.d("Last location: $location")
        location?.let { sendLocation(it) }
      }
      .addOnFailureListener { e -> Timber.e(e, "Failed to get last location.") }

    val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          Timber.d("Location update: ${locationResult.lastLocation}")
          locationResult.lastLocation?.let { sendLocation(it) }
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

    private val TICKER_INTERVAL = 15.seconds
    private val FLOW_TIMEOUT_MILLIS = 5.seconds.inWholeMilliseconds

    private val LOCATION_UPDATE_INTERVAL_MILLIS = 2.minutes.inWholeMilliseconds
    private val MIN_LOCATION_UPDATE_INTERVAL_MILLIS = 30.seconds.inWholeMilliseconds

    private val LOCATION_REQUEST =
      LocationRequest.Builder(
          Priority.PRIORITY_BALANCED_POWER_ACCURACY,
          LOCATION_UPDATE_INTERVAL_MILLIS,
        )
        .setMinUpdateIntervalMillis(MIN_LOCATION_UPDATE_INTERVAL_MILLIS)
        .build()

    private fun ProducerScope<Location>.sendLocation(location: Location) {
      trySend(location)
        .onSuccess { Timber.d("Location sent to flow.") }
        .onFailure { t -> Timber.e(t, "Failed to send location to flow.") }
    }
  }
}
