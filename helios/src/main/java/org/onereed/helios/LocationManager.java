package org.onereed.helios;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.onereed.helios.common.LocationUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.ToastUtil;

import java.util.Arrays;
import java.util.function.Consumer;

class LocationManager implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(LocationManager.class);

  private static final int REQUEST_PERMISSION_CODE = 1;
  private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

  private final Activity activity;

  private FusedLocationProviderClient fusedLocationClient;
  private HandlerThread locationHandlerThread;

  LocationManager(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onCreate");

    fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(activity.getApplicationContext());

    locationHandlerThread = new HandlerThread("LocationUpdates");
    locationHandlerThread.start();
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    Log.d(TAG, "onDestroy");
    locationHandlerThread.quitSafely();
  }

  void requestLocation(Consumer<Location> locationConsumer) {
    LocationRequest locationRequest = LocationUtil.createOneShotLocationRequest();
    LocationCallback locationCallback = new LocationUpdateRecipient(locationConsumer);

    runWithPermission(
        () ->
            fusedLocationClient
                .requestLocationUpdates(
                    locationRequest, locationCallback, locationHandlerThread.getLooper())
                .addOnSuccessListener(unused -> Log.d(TAG, "Location update requested."))
                .addOnFailureListener(e -> Log.e(TAG, "Location update request failed.", e)));
  }

  private void runWithPermission(Runnable runnable) {
    int permissionResult =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

    if (permissionResult == PackageManager.PERMISSION_GRANTED) {
      runnable.run();
    } else {
      Log.d(TAG, "Requesting location permission.");
      ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_PERMISSION_CODE);
    }
  }

  /**
   * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])} with the same
   * arguments. We do nothing but log the result in this method; requesting permission necessarily
   * leads to the app being paused, and onResume in various components will issue new requests
   * needing location permission which should now succeed directly.
   */
  void acceptPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    if (requestCode != REQUEST_PERMISSION_CODE || !Arrays.equals(PERMISSIONS, permissions)) {
      return; // Not our message.
    }

    // If request is cancelled, the result arrays are empty.
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "User granted request for location permission.");
    } else {
      Log.w(TAG, "User refused request for location permission.");
      ToastUtil.longToast(activity, R.string.toast_location_permission_needed);
    }
  }

  private class LocationUpdateRecipient extends LocationCallback {

    private final Consumer<Location> locationConsumer;

    private LocationUpdateRecipient(Consumer<Location> locationConsumer) {
      this.locationConsumer = locationConsumer;
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
      if (!locationAvailability.isLocationAvailable()) {
        Log.w(TAG, "Location not available.");
      }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
      Log.d(TAG, "LocationResult obtained.");
      locationConsumer.accept(locationResult.getLastLocation());
      fusedLocationClient
          .removeLocationUpdates(this)
          .addOnSuccessListener(unused -> Log.d(TAG, "Removed location update."))
          .addOnFailureListener(e -> Log.w(TAG, "Failed to remove location update.", e));
    }
  }
}
