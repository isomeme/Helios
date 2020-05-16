package org.onereed.helios;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEngine;
import org.onereed.helios.sun.SunInfo;

import java.time.Clock;
import java.util.concurrent.Executors;

/**
 * Stores and updates data needed for {@link SunInfo} display.
 */
@SuppressWarnings("WeakerAccess") // Must be public for ViewModelProvider.
public class SunInfoViewModel extends ViewModel {

  private final String TAG = LogUtil.makeTag(SunInfoViewModel.class);

  private static final Clock CLOCK = Clock.systemUTC();

  private final SunEngine sunEngine = new SunEngine(CLOCK);

  private final MutableLiveData<SunInfo> sunInfoMutableLiveData = new MutableLiveData<>();

  private Location lastLocation = null;

  LiveData<SunInfo> getSunInfoLiveData() {
    return sunInfoMutableLiveData;
  }

  void acceptLocation(@NonNull Location location) {
    AppLogger.debug(TAG, "Accepting location=%s", location);
    lastLocation = location;
    // TODO: Find a better way.
    Executors.newSingleThreadExecutor().submit(this::updateSunInfo);
  }

  private void updateSunInfo() {
    SunInfo sunInfo = sunEngine.locationToSunInfo(lastLocation);
    sunInfoMutableLiveData.postValue(sunInfo);
  }
}
