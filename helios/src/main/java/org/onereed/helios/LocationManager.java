package org.onereed.helios;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;

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
import org.onereed.helios.logger.AppLogger;

import java.util.Arrays;
import java.util.function.Consumer;

class LocationManager implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(LocationManager.class);

  private static final int REQUEST_PERMISSION_CODE = 1;
  private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

  private final LocationUpdateRecipient locationUpdateRecipient = new LocationUpdateRecipient();

  private final Activity activity;
  private final Consumer<Location> locationConsumer;

  private FusedLocationProviderClient fusedLocationClient;
  private HandlerThread locationHandlerThread;

  LocationManager(Activity activity, Consumer<Location> locationConsumer) {
    this.activity = activity;
    this.locationConsumer = locationConsumer;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    AppLogger.debug(TAG, "onCreate");

    fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(activity.getApplicationContext());

    locationHandlerThread = new HandlerThread("LocationUpdates");
    locationHandlerThread.start();
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    AppLogger.debug(TAG, "onResume");

    if (checkPermission()) {
      LocationRequest locationRequest = LocationUtil.createRepeatedLocationRequest();
      fusedLocationClient
          .requestLocationUpdates(
              locationRequest, locationUpdateRecipient, locationHandlerThread.getLooper())
          .addOnFailureListener(e -> AppLogger.error(TAG, e, "Location update request failed."));
    }
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    AppLogger.debug(TAG, "onPause");

    fusedLocationClient
        .removeLocationUpdates(locationUpdateRecipient)
        .addOnFailureListener(e -> AppLogger.error(TAG, e, "Failed to remove location update."));
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    AppLogger.debug(TAG, "onDestroy");
    locationHandlerThread.quitSafely();
  }

  void requestLastLocation() {
    AppLogger.debug(TAG, "requestLastLocation");
    fusedLocationClient.getLastLocation()
        .addOnSuccessListener(locationConsumer::accept)
        .addOnFailureListener(e -> AppLogger.error(TAG, e, "getLastLocation failed."));
  }

  /**
   * Returns {@code true} if we already have all needed location permissions. A {@code false} return
   * value indicates that we are initiating a user permission request, meaning that the activity
   * will soon be paused.
   */
  private boolean checkPermission() {
    int permissionResult =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

    boolean permissionOkay = permissionResult == PackageManager.PERMISSION_GRANTED;

    if (!permissionOkay) {
      AppLogger.debug(TAG, "Requesting location permission.");
      ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_PERMISSION_CODE);
    }

    return permissionOkay;
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
      AppLogger.debug(TAG, "User granted request for location permission.");
    } else {
      AppLogger.warning(TAG, "User refused request for location permission.");
      ToastUtil.longToast(activity, R.string.toast_location_permission_needed);
    }
  }

  private class LocationUpdateRecipient extends LocationCallback {

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
      if (!locationAvailability.isLocationAvailable()) {
        AppLogger.warning(TAG, "Location not available.");
      }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
      locationConsumer.accept(locationResult.getLastLocation());
    }
  }
}
