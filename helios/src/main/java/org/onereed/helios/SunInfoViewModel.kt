package org.onereed.helios

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationListener
import com.google.android.gms.tasks.Task
import org.onereed.helios.common.Place
import org.onereed.helios.sun.SunInfo
import org.onereed.helios.sun.SunInfoSource
import timber.log.Timber
import java.time.Instant

/** Stores and updates data needed for [SunInfo] display. */
class SunInfoViewModel : ViewModel(), LocationListener {

  private val sunInfoMutableLiveData = MutableLiveData<SunInfo>()

  val sunInfoLiveData: LiveData<SunInfo>
    get() = sunInfoMutableLiveData

  override fun onLocationChanged(location: Location) {
    val place = Place(location)
    SunInfoSource.request(place, Instant.now()).addOnCompleteListener { publishSunInfo(it) }
  }

  private fun publishSunInfo(sunInfoTask: Task<SunInfo>) {
    if (sunInfoTask.isSuccessful) {
      sunInfoMutableLiveData.postValue(sunInfoTask.result)
    } else {
      Timber.e(sunInfoTask.exception, "Failure obtaining SunInfo.")
    }
  }
}
