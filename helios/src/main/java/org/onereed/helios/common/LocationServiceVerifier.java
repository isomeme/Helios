package org.onereed.helios.common;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import org.onereed.helios.R;

/**
 * When activity resumes, checks that LocationService is on and capable of handling our request for
 * updates. If not, it attempts to assist the user in fixing this problem.
 */
public class LocationServiceVerifier implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(LocationServiceVerifier.class);

  /** The specific value doesn't matter; the API just needs some int. */
  private static final int REQUEST_CHECK_SETTINGS_CODE = 2;

  private final Activity activity;
  private SettingsClient settingsClient;

  public LocationServiceVerifier(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    settingsClient = LocationServices.getSettingsClient(activity.getApplicationContext());
  }

  /**
   * Determines whether Google Play Services is available; calls {@link Activity#finish()} on {@link
   * #activity} if not.
   */
  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    LocationSettingsRequest locationSettingsRequest =
        new LocationSettingsRequest.Builder()
            .addLocationRequest(LocationUtil.REPEATED_LOCATION_REQUEST)
            .build();

    settingsClient
        .checkLocationSettings(locationSettingsRequest)
        .addOnCompleteListener(
            task -> {
              try {
                task.getResult(ApiException.class);
              } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                  case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                      ResolvableApiException resolvable = (ResolvableApiException) exception;
                      resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS_CODE);
                    } catch (IntentSender.SendIntentException | ClassCastException e) {
                      Log.e(TAG, "Unexpected exception", e);
                      ToastUtil.longToastAndFinish(
                          activity, R.string.toast_locationservice_not_available);
                    }
                    break;
                  case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e(TAG, "Location settings change unavailable.");
                    ToastUtil.longToastAndFinish(
                        activity, R.string.toast_locationservice_not_available);
                    break;
                }
              }
            });
  }

  public void acceptActivityResult(int requestCode, int resultCode) {
    if (requestCode == REQUEST_CHECK_SETTINGS_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Log.i(TAG, "User enabled LocationServices.");
      } else {
        Log.e(TAG, "resultCode != RESULT_OK : " + resultCode);
        ToastUtil.longToastAndFinish(activity, R.string.toast_locationservice_not_available);
      }
    }
  }
}
