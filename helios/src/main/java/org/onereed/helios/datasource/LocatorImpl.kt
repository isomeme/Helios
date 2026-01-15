package org.onereed.helios.datasource

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
import javax.inject.Inject
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.onereed.helios.common.ApplicationScope
import org.onereed.helios.common.stateIn
import org.onereed.helios.datasource.PlaceTime.Place
import timber.log.Timber

@OptIn(ExperimentalTime::class)
class LocatorImpl
@Inject
constructor(
  @param:ApplicationContext private val context: Context,
  @param:ApplicationScope private val externalScope: CoroutineScope,
) : Locator {
  private val locationProvider by lazy { LocationServices.getFusedLocationProviderClient(context) }

  private val ticker = Ticker(TICKER_INTERVAL, "LocatorTicker")

  private val _placeTimeFlow =
    getLocationUpdates()
      .map(::Place)
      .combine(ticker.flow) { place, _ -> PlaceTime(place, now()) }
      .onStart { Timber.d("Locator flow start") }
      .onEach { Timber.d("Locator flow onEach $it") }
      .onCompletion { Timber.d("Locator flow stop") }
      .stateIn(scope = externalScope, initialValue = PlaceTime.EMPTY, stopTimeout = 5.seconds)

  override fun placeTimeFlow() = _placeTimeFlow

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
    if (context.checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
      Timber.d("Not requesting location updates; permission not granted yet.")
      close()
      return@callbackFlow
    }

    val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) { locationResult.lastLocation?.let {
            trySend(it).onFailure { t -> Timber.e(t, "Failed to send location to flow.") }
          }
        }
      }

    locationProvider
      .requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper())
      .addOnSuccessListener { Timber.d("Location updates requested.") }
      .addOnFailureListener { e ->
        Timber.e(e, "Location updates request failed.")
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

    private val LOCATION_UPDATE_INTERVAL_MILLIS = 2.minutes.inWholeMilliseconds
    private val MIN_LOCATION_UPDATE_INTERVAL_MILLIS = 30.seconds.inWholeMilliseconds

    private val LOCATION_REQUEST =
      LocationRequest.Builder(
          Priority.PRIORITY_BALANCED_POWER_ACCURACY,
          LOCATION_UPDATE_INTERVAL_MILLIS,
        )
        .setMinUpdateIntervalMillis(MIN_LOCATION_UPDATE_INTERVAL_MILLIS)
        .build()
  }
}
