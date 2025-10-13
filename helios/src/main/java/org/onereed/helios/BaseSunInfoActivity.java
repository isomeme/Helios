package org.onereed.helios;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.onereed.helios.sun.SunInfo;

import java.time.Duration;
import java.util.concurrent.Executor;
import timber.log.Timber;

abstract class BaseSunInfoActivity extends BaseActivity {

  private static final Duration UPDATE_INTERVAL = Duration.ofSeconds(30L);
  private static final Duration MIN_UPDATE_INTERVAL = Duration.ofSeconds(15L);

  private static final LocationRequest REPEATED_LOCATION_REQUEST =
      new LocationRequest.Builder(
              Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL.toMillis())
          .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL.toMillis())
          .build();

  private SunInfoViewModel sunInfoViewModel;

  private FusedLocationProviderClient fusedLocationProviderClient;
  private Executor mainExecutor;

  private ActivityResultLauncher<String> requestPermissionLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    sunInfoViewModel = new ViewModelProvider(this).get(SunInfoViewModel.class);

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    mainExecutor = ContextCompat.getMainExecutor(this);

    requestPermissionLauncher =
        registerForActivityResult(new RequestPermission(), this::acceptLocationPermissionResult);
  }

  @Override
  protected void onStart() {
    Timber.d("onStart");
    super.onStart();

    if (checkLocationPermission()) {
      Timber.d("Location permission already granted");
    } else {
      Timber.d("Requesting location permission");
      requestLocationPermission();
    }
  }

  @Override
  protected void onResume() {
    Timber.d("onResume");
    super.onResume();

    if (checkLocationPermission()) {
      Timber.d("About to request location updates.");

      fusedLocationProviderClient
          .requestLocationUpdates(REPEATED_LOCATION_REQUEST, mainExecutor, sunInfoViewModel)
          .addOnSuccessListener(unusedVoid -> Timber.d("Location updates started."))
          .addOnFailureListener(e -> Timber.e(e, "Location updates start failed."));

      // TODO: Add failure indicator to UI.
    } else {
      Timber.d("Not requesting location updates; permission not granted yet.");
    }
  }

  @Override
  protected void onPause() {
    Timber.d("onPause");
    super.onPause();

    fusedLocationProviderClient
        .removeLocationUpdates(sunInfoViewModel)
        .addOnSuccessListener(unusedVoid -> Timber.d("Location updates stopped."))
        .addOnFailureListener(e -> Timber.e(e, "Location updates stop failed."));
  }

  protected void observeSunInfo(Observer<SunInfo> sunInfoObserver) {
    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoObserver);
  }

  private void acceptLocationPermissionResult(boolean isGranted) {
    Timber.d("acceptLocationPermissionResult: isGranted=%b", isGranted);

    if (isGranted) {
      return;
    }

    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
      new AlertDialog.Builder(this)
          .setMessage(R.string.location_permission_rationale)
          .setPositiveButton(
              R.string.button_continue, (dialog, which) -> requestLocationPermission())
          .setNegativeButton(R.string.button_exit, (dialog, which) -> finish())
          .setCancelable(false)
          .create()
          .show();

      Timber.d("Launched rationale dialog.");
    } else {
      Intent settingsIntent =
          new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
              .setData(Uri.fromParts("package", getPackageName(), /* fragment= */ null));

      new AlertDialog.Builder(this)
          .setMessage(R.string.location_permission_use_settings)
          .setPositiveButton(
              R.string.button_settings, (dialog, which) -> startActivity(settingsIntent))
          .setNegativeButton(R.string.button_exit, (dialog, which) -> finish())
          .setCancelable(false)
          .create()
          .show();

      Timber.d("Launched use-settings dialog.");
    }
  }

  protected boolean checkLocationPermission() {
    return checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private void requestLocationPermission() {
    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION);
  }
}
