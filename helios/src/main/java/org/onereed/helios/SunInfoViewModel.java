package org.onereed.helios;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunInfo;
import org.onereed.helios.sun.SunInfoSource;

import java.time.Clock;

/**
 * Stores and updates data needed for {@link SunInfo} display.
 */
class SunInfoViewModel extends ViewModel {

  private static final String TAG = LogUtil.makeTag(SunInfoViewModel.class);

  private static final Clock CLOCK = Clock.systemUTC();

  private final MutableLiveData<SunInfo> sunInfoMutableLiveData = new MutableLiveData<>();

  private Location lastLocation = null;

  LiveData<SunInfo> getSunInfoLiveData() {
    return sunInfoMutableLiveData;
  }

  void acceptLocation(@NonNull Location location) {
    AppLogger.debug(TAG, "Accepting location=%s", location);
    lastLocation = location;
    updateSunInfo();
  }

  private void updateSunInfo() {
    SunInfoSource.request(lastLocation.getLatitude(), lastLocation.getLongitude(), CLOCK.instant())
        .addOnSuccessListener(sunInfoMutableLiveData::postValue)
        .addOnFailureListener(e -> AppLogger.error(TAG, e, "Failure obtaining SunInfo."));
  }
}
