@file:OptIn(ExperimentalCoroutinesApi::class)

package org.onereed.helios.datasource

import android.content.Context
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import org.onereed.helios.common.arc
import timber.log.Timber

@Singleton
class Orienter
@Inject
constructor(@ApplicationContext context: Context, storeRepository: StoreRepository) {

  private val executor by lazy { Dispatchers.Default.asExecutor() }

  private val orientationProvider by lazy {
    LocationServices.getFusedOrientationProviderClient(context)
  }

  private val lockedHeadingFlow =
    storeRepository.isCompassSouthTopFlow.map { southTop -> if (southTop) 180f else 0f }

  private val swingToLockedHeadingFlow =
    lockedHeadingFlow
      .flatMapLatest { heading -> repeatingTickerFlow(TICKER_INTERVAL, heading) }
      .take(LOCK_SWING_TICKS)

  private val liveHeadingFlow = getOrientationUpdates().map { it.headingDegrees }

  val headingFlow =
    lockedHeadingFlow.flatMapLatest { lockedHeading ->
      storeRepository.isCompassLockedFlow
        .flatMapLatest { isLocked -> if (isLocked) swingToLockedHeadingFlow else liveHeadingFlow }
        .scan(lockedHeading, ::smooth)
        .map(::quantize)
    }

  private fun getOrientationUpdates(): Flow<DeviceOrientation> = callbackFlow {
    val orientationListener = DeviceOrientationListener {
      trySend(it).onFailure { t -> Timber.e(t, "Failed to send orientation to flow.") }
    }

    orientationProvider
      .requestOrientationUpdates(DEVICE_ORIENTATION_REQUEST, executor, orientationListener)
      .addOnSuccessListener { Timber.d("Orientation updates requested.") }
      .addOnFailureListener { e ->
        Timber.e(e, "Orientation updates request failed.")
        close(e)
      }

    awaitClose {
      orientationProvider
        .removeOrientationUpdates(orientationListener)
        .addOnSuccessListener { Timber.d("Orientation updates removed.") }
        .addOnFailureListener { e -> Timber.e(e, "Orientation updates removal failed.") }
    }
  }

  private companion object {

    private fun smooth(previous: Float, next: Float): Float {
      val shortestDelta = arc(from = previous, to = next)
      val smoothed = previous + ALPHA * shortestDelta
      return smoothed.mod(360f) // Normalize angle into [0..360)
    }

    private fun quantize(value: Float): Float = (value / QUANTUM).roundToInt() * QUANTUM

    /** The low-pass filter weighting parameter. */
    private const val ALPHA = 0.15f

    /** The granularity of heading angles. */
    private const val QUANTUM = 0.5f

    private val DEVICE_ORIENTATION_REQUEST =
      DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()

    /**
     * The ticker that animates swinging the compass into locked position needs to fire at the same
     * rate as orientation updates.
     */
    private val TICKER_INTERVAL = DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT.microseconds
    private val LOCK_SWING_INTERVAL = 3.seconds
    private val LOCK_SWING_TICKS = (LOCK_SWING_INTERVAL / TICKER_INTERVAL).toInt()
  }
}
