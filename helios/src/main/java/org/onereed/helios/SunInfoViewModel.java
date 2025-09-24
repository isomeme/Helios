package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import java.time.Clock;
import java.time.Instant;
import org.onereed.helios.common.Place;
import org.onereed.helios.sun.SunInfo;
import org.onereed.helios.sun.SunInfoSource;
import timber.log.Timber;

/** Stores and updates data needed for {@link SunInfo} display. */
// Must be public to work with the default ViewModel provider factory.
public class SunInfoViewModel extends ViewModel {

  private static final Clock CLOCK = Clock.systemUTC();

  private final MutableLiveData<SunInfo> sunInfoMutableLiveData = new MutableLiveData<>();

  /** This will always be updated for every {@link SunInfo} request, even if the request fails. */
  private final MutableLiveData<Instant> lastUpdateTimeMutableLiveData = new MutableLiveData<>();

  LiveData<SunInfo> getSunInfoLiveData() {
    return sunInfoMutableLiveData;
  }

  LiveData<Instant> getLastUpdateTimeLiveData() {
    return lastUpdateTimeMutableLiveData;
  }

  void acceptPlace(Place where) {
    SunInfoSource.request(where, CLOCK.instant()).addOnCompleteListener(this::publishSunInfo);
  }

  private void publishSunInfo(Task<SunInfo> sunInfoTask) {
    lastUpdateTimeMutableLiveData.postValue(CLOCK.instant());

    if (sunInfoTask.isSuccessful()) {
      var sunInfo = checkNotNull(sunInfoTask.getResult());
      sunInfoMutableLiveData.postValue(sunInfo);
    } else {
      Timber.e(sunInfoTask.getException(), "Failure obtaining SunInfo.");
    }
  }
}
