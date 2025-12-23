package org.onereed.helios.common

import android.content.Context
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@Singleton
class Orienter @Inject constructor(@param:ApplicationContext private val context: Context) {

  private val executor by lazy { Dispatchers.Default.asExecutor() }

  private val orientationProvider by lazy {
    LocationServices.getFusedOrientationProviderClient(context)
  }

  val flow =
    getOrientationUpdates()
      .map { it.headingDegrees }
      .onStart { Timber.d("Orienter.onStart") }
      .onCompletion { Timber.d("Orienter.onCompletion") }

  private fun getOrientationUpdates(): Flow<DeviceOrientation> = callbackFlow {
    val orientationListener = DeviceOrientationListener {
      trySend(it).onFailure { t -> Timber.e(t, "Failed to send orientation to flow.") }
    }

    orientationProvider
      .requestOrientationUpdates(DEVICE_ORIENTATION_REQUEST, executor, orientationListener)
      .addOnSuccessListener { Timber.d("Orientation updates started.") }
      .addOnFailureListener { e ->
        Timber.e(e, "Orientation updates start failed.")
        close(e)
      }

    awaitClose {
      orientationProvider
        .removeOrientationUpdates(orientationListener)
        .addOnSuccessListener { Timber.d("Orientation updates stopped.") }
        .addOnFailureListener { e -> Timber.e(e, "Orientation updates stop failed.") }
    }
  }

  private companion object {

    private val DEVICE_ORIENTATION_REQUEST =
      DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()
  }
}
