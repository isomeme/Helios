package org.onereed.helios;

import android.app.Activity;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.ToastUtil;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.util.function.Consumer;

/** Obtains sun position info and hands it off to a UI-thread consumer. */
class SunHandler {

  private static final String TAG = LogUtil.makeTag(SunHandler.class);

  private final Activity activity;
  private final LocationManager locationManager;
  private final Consumer<SunTimes> sunTimesConsumer;

  SunHandler(
      Activity activity, LocationManager locationManager, Consumer<SunTimes> sunTimesConsumer) {

    this.activity = activity;
    this.locationManager = locationManager;
    this.sunTimesConsumer = sunTimesConsumer;
  }

  void update() {
    locationManager.requestLocation(this::acceptLocation);
  }

  private void acceptLocation(Location location) {
    if (location == null) {
      Log.e(TAG, "Location is null.");
      ToastUtil.longToast(activity, R.string.toast_location_failure);
      return;
    }

    long nowElapsedNanos = SystemClock.elapsedRealtimeNanos();
    long fixElapsedNanos = location.getElapsedRealtimeNanos();
    long ageNanos = nowElapsedNanos - fixElapsedNanos;
    Duration age = Duration.ofNanos(ageNanos);

    double lat = location.getLatitude();
    double lon = location.getLongitude();

    Log.d(TAG, String.format("lat=%f lon=%f age=%s", lat, lon, age));

    SunTimes sunTimes = SunTimes.compute().at(lat, lon).execute();
    sunTimesConsumer.accept(sunTimes);
  }
}
