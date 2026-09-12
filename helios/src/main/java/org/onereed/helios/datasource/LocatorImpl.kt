package org.onereed.helios.datasource

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import org.onereed.helios.common.ApplicationScope
import org.onereed.helios.datasource.PlaceTime.Place
import org.onereed.shared.logging.logAllEvents
import org.onereed.shared.logging.logOutcomes
import org.onereed.shared.permission.hasLocationPermission
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LocatorImpl
@Inject
constructor(
  @ApplicationScope private val externalScope: CoroutineScope,
  @ApplicationContext private val context: Context,
) : Locator {

  private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

  private val ticker = unitTickerFlow(TICKER_INTERVAL)

  private val _placeTimeFlow =
    getLocationUpdates()
      .onStart {
        if (context.hasLocationPermission()) {
          // Fetch the hardware's last known location to bootstrap the UI instantly

          @Suppress("MissingPermission")
          val hardwareCache = locationClient.lastLocation.await()
          if (hardwareCache != null) emit(hardwareCache)
        }
      }
      .map(::Place)
      .combine(ticker) { place, _ -> PlaceTime(place, now()) }
      .logAllEvents("placeTimeFlow")
      .shareIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MILLIS),
        replay = 1
      )

  override fun placeTimeFlow() = _placeTimeFlow

  private fun getLocationUpdates(): Flow<Location> = callbackFlow {
    if (!context.hasLocationPermission()) {
      Timber.d("Location reporting is not yet available.")
      close()
      return@callbackFlow
    }

    val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          locationResult.lastLocation?.let {
            trySend(it).onFailure { t -> Timber.e(t, "Failed to send location update to flow.") }
          }
        }
      }

    @Suppress("MissingPermission")
    locationClient
      .requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper())
      .logOutcomes("requestLocationUpdates")
      .addOnFailureListener { e -> close(e) }

    awaitClose {
      locationClient.removeLocationUpdates(locationCallback).logOutcomes("removeLocationUpdates")
    }
  }

  companion object {

    private val TICKER_INTERVAL = 15.seconds

    private val SHARING_TIMEOUT_MILLIS = 5.seconds.inWholeMilliseconds

    private val LOCATION_UPDATE_INTERVAL_MILLIS = 2.minutes.inWholeMilliseconds

    private val LOCATION_REQUEST =
      LocationRequest.Builder(
          Priority.PRIORITY_BALANCED_POWER_ACCURACY,
          LOCATION_UPDATE_INTERVAL_MILLIS,
        )
        .build()
  }
}
