package org.onereed.helios;

import android.app.Activity;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.ToastUtil;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Obtains sun position info and hands it off to a consumer. */
class SunCalculator {

  private static final String TAG = LogUtil.makeTag(SunCalculator.class);

  private final Executor executor = Executors.newSingleThreadExecutor();

  private final Activity activity;
  private final LocationManager locationManager;
  private final Consumer<SunTimes> sunTimesConsumer;

  SunCalculator(
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

    executor.execute(() -> locationToSunInfo(location));
  }

  private void locationToSunInfo(@NonNull Location location) {
    long nowElapsedNanos = SystemClock.elapsedRealtimeNanos();
    long fixElapsedNanos = location.getElapsedRealtimeNanos();
    long ageNanos = nowElapsedNanos - fixElapsedNanos;
    Duration age = Duration.ofNanos(ageNanos);

    double lat = location.getLatitude();
    double lon = location.getLongitude();

    Log.d(TAG, String.format("lat=%f lon=%f age=%s", lat, lon, age));

    SunTimes sunTimes = SunTimes.compute().today().at(lat, lon).execute();
    sunTimesConsumer.accept(sunTimes);
  }
}
