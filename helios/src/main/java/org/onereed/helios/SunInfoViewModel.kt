package org.onereed.helios

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationListener
import com.google.android.gms.tasks.Task
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.onereed.helios.common.Place
import org.onereed.helios.sun.SunInfo
import org.onereed.helios.sun.SunInfoSource
import timber.log.Timber

/** Stores and updates data needed for [SunInfo] display. */
class SunInfoViewModel : ViewModel(), LocationListener {

  private val _sunInfo = MutableStateFlow<SunInfo?>(null)

  val sunInfo = _sunInfo.asStateFlow()

  fun acceptLocation(location: Location) {
    onLocationChanged(location)
  }

  override fun onLocationChanged(location: Location) {
    val place = Place(location)
    SunInfoSource.request(place, Instant.now()).addOnCompleteListener { publishSunInfo(it) }
  }

  private fun publishSunInfo(sunInfoTask: Task<SunInfo>) {
    if (sunInfoTask.isSuccessful) {
      _sunInfo.value = sunInfoTask.result
    } else {
      Timber.e(sunInfoTask.exception, "Failure obtaining SunInfo.")
    }
  }
}
