package org.onereed.helios;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunInfo;
import org.onereed.helios.sun.SunInfoSource;

import java.time.Clock;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

/** Stores and updates data needed for {@link SunInfo} display. */
class SunInfoViewModel extends ViewModel {

  private static final String TAG = LogUtil.makeTag(SunInfoViewModel.class);

  private static final Clock CLOCK = Clock.systemUTC();

  private final MutableLiveData<SunInfo> sunInfoMutableLiveData = new MutableLiveData<>();

  /** This will always be updated for every {@link SunInfo} request, even if the request fails. */
  private final MutableLiveData<Instant> lastUpdateTimeMutableLiveData = new MutableLiveData<>();

  private Location lastLocation = null;

  LiveData<SunInfo> getSunInfoLiveData() {
    return sunInfoMutableLiveData;
  }

  LiveData<Instant> getLastUpdateTimeLiveData() {
    return lastUpdateTimeMutableLiveData;
  }

  void acceptLocation(Location location) {
    // Location has never been null in emulator or device tests, but it was null on all automated
    // Play Store acceptance tests. So we will handle it gracefully.

    if (location != null) {
      lastLocation = location;
      updateSunInfo();
    }
  }

  private void updateSunInfo() {
    SunInfoSource.request(lastLocation.getLatitude(), lastLocation.getLongitude(), CLOCK.instant())
        .addOnCompleteListener(this::publishSunInfo);
  }

  private void publishSunInfo(Task<SunInfo> sunInfoTask) {
    lastUpdateTimeMutableLiveData.postValue(CLOCK.instant());

    if (sunInfoTask.isSuccessful()) {
      SunInfo sunInfo = checkNotNull(sunInfoTask.getResult());
      sunInfoMutableLiveData.postValue(sunInfo);
    } else {
      AppLogger.error(TAG, sunInfoTask.getException(), "Failure obtaining SunInfo.");
    }
  }
}
