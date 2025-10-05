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
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.concurrent.Executor;
import org.onereed.helios.common.LocationUtil;
import timber.log.Timber;

abstract class BaseSunInfoActivity extends BaseActivity {

  protected SunInfoViewModel sunInfoViewModel;
  protected Executor mainExecutor;

  private FusedLocationProviderClient fusedLocationProviderClient;
  private ActivityResultLauncher<String> requestPermissionLauncher;

  private Intent settingsIntent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    sunInfoViewModel = new ViewModelProvider(this).get(SunInfoViewModel.class);
    mainExecutor = ContextCompat.getMainExecutor(this);

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    requestPermissionLauncher =
        registerForActivityResult(new RequestPermission(), this::acceptLocationPermissionResult);

    settingsIntent =
        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", getPackageName(), /* fragment= */ null));
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
          .requestLocationUpdates(
              LocationUtil.REPEATED_LOCATION_REQUEST, mainExecutor, sunInfoViewModel)
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

  private void acceptLocationPermissionResult(boolean isGranted) {
    Timber.d("acceptLocationPermissionResult: isGranted=%b", isGranted);

    if (isGranted) {
      return;
    }

    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
      new AlertDialog.Builder(this)
          .setMessage("Pretty please??")
          .setPositiveButton("Continue", (dialog, which) -> requestLocationPermission())
          .setNegativeButton("Exit", (dialog, which) -> finish())
          .setCancelable(false)
          .create()
          .show();

      Timber.d("Launched rationale dialog.");
    } else {
      new AlertDialog.Builder(this)
          .setMessage("You need to fix this in settings.")
          .setPositiveButton("Settings", (dialog, which) -> startActivity(settingsIntent))
          .setNegativeButton("Exit", (dialog, which) -> finish())
          .setCancelable(false)
          .create()
          .show();

      Timber.d("Launched go-to-settings dialog.");
    }
  }

  protected boolean checkLocationPermission() {
    return checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private void requestLocationPermission() {
    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION);
  }
}
