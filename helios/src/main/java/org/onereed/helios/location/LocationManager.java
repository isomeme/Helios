package org.onereed.helios.location;

import static org.onereed.helios.common.ToastUtil.longToast;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.onereed.helios.R;
import org.onereed.helios.common.LocationUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.Place;
import org.onereed.helios.logger.AppLogger;

import java.util.Arrays;
import java.util.function.Consumer;

public class LocationManager implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(LocationManager.class);

  private static final int REQUEST_PERMISSION_CODE = 1;
  private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

  private final LocationUpdateRecipient locationUpdateRecipient = new LocationUpdateRecipient();

  private final Activity activity;
  private final Consumer<Place> placeConsumer;

  private FusedLocationProviderClient fusedLocationClient;
  private HandlerThread locationHandlerThread;

  public LocationManager(Activity activity, Consumer<Place> placeConsumer) {
    this.activity = activity;
    this.placeConsumer = placeConsumer;
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
      fusedLocationClient
          .requestLocationUpdates(
              LocationUtil.REPEATED_LOCATION_REQUEST,
              locationUpdateRecipient,
              locationHandlerThread.getLooper())
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

  public void requestLastLocation() {
    AppLogger.debug(TAG, "requestLastLocation");

    if (checkPermission()) {
      fusedLocationClient
          .getLastLocation()
          .addOnSuccessListener(this::relayPlace)
          .addOnFailureListener(e -> AppLogger.error(TAG, e, "getLastLocation failed."));
    }
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
  public void acceptPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    if (requestCode != REQUEST_PERMISSION_CODE || !Arrays.equals(PERMISSIONS, permissions)) {
      return; // Not our message.
    }

    // If request is cancelled, the result arrays are empty.
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      AppLogger.debug(TAG, "User granted request for location permission.");
    } else {
      AppLogger.warning(TAG, "User refused request for location permission.");
      longToast(activity, R.string.toast_location_permission_needed);
    }
  }

  /**
   * Converts a received {@link Location} update into a {@link Place} value and relays it to the
   * registered consumer.
   */
  private void relayPlace(@Nullable Location location) {
    // Location has never been null in emulator or device tests, but it was null on all automated
    // Play Store acceptance tests. So we will handle it gracefully.

    if (location != null) {
      placeConsumer.accept(Place.from(location));
    } else {
      AppLogger.warning(TAG, "Null location received.");
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
      relayPlace(locationResult.getLastLocation());
    }
  }
}
