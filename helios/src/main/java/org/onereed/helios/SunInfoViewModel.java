package org.onereed.helios;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;
import org.onereed.helios.sun.SunInfoSource;

import java.time.Clock;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

/** Stores and updates data needed for {@link SunInfo} display. */
class SunInfoViewModel extends ViewModel {

  private static final String TAG = LogUtil.makeTag(SunInfoViewModel.class);

  private static final Clock CLOCK = Clock.systemUTC();

  private final MutableLiveData<ImmutableList<SunEvent>> sunEventsMutableLiveData =
      new MutableLiveData<>();

  /** This will always be updated for every {@link SunInfo} request, even if the request fails. */
  private final MutableLiveData<Instant> lastUpdateTimeMutableLiveData = new MutableLiveData<>();

  private Location lastLocation = null;

  LiveData<ImmutableList<SunEvent>> getSunEventsLiveData() {
    return sunEventsMutableLiveData;
  }

  LiveData<Instant> getLastUpdateTimeLiveData() {
    return lastUpdateTimeMutableLiveData;
  }

  void acceptLocation(@NonNull Location location) {
    AppLogger.debug(TAG, "Accepting location=%s", location);
    lastLocation = location;
    updateSunInfo();
  }

  private void updateSunInfo() {
    SunInfoSource.request(lastLocation.getLatitude(), lastLocation.getLongitude(), CLOCK.instant())
        .addOnCompleteListener(this::publishSunInfo);
  }

  private void publishSunInfo(Task<SunInfo> sunInfoTask) {
    lastUpdateTimeMutableLiveData.postValue(CLOCK.instant());

    if (sunInfoTask.isSuccessful()) {
      SunInfo sunInfo = checkNotNull(sunInfoTask.getResult());
      sunEventsMutableLiveData.postValue(sunInfo.getSunEvents());
    } else {
      AppLogger.error(TAG, sunInfoTask.getException(), "Failure obtaining SunInfo.");
    }
  }
}
