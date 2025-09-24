package org.onereed.helios;

import static org.onereed.helios.common.ToastUtil.longToastAndFinish;

import android.app.Activity;
import android.content.IntentSender;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import org.onereed.helios.common.LocationUtil;
import timber.log.Timber;

/**
 * When activity resumes, checks that LocationService is on and capable of handling our request for
 * updates. If not, it attempts to assist the user in fixing this problem.
 */
class LocationServiceVerifier implements DefaultLifecycleObserver {

  /** The specific value doesn't matter; the API just needs some int. */
  private static final int REQUEST_CHECK_SETTINGS_CODE = 2;

  private final Activity activity;
  private SettingsClient settingsClient;

  LocationServiceVerifier(Activity activity) {
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
                      Timber.e(e, "Unexpected exception");
                      longToastAndFinish(
                          activity, R.string.toast_locationservice_not_available);
                    }
                    break;
                  case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Timber.e("Location settings change unavailable.");
                    longToastAndFinish(
                        activity, R.string.toast_locationservice_not_available);
                    break;
                }
              }
            });
  }

  void acceptActivityResult(int requestCode, int resultCode) {
    if (requestCode == REQUEST_CHECK_SETTINGS_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Timber.i("User enabled LocationServices.");
      } else {
        Timber.e("resultCode != RESULT_OK : %s", resultCode);
        longToastAndFinish(activity, R.string.toast_locationservice_not_available);
      }
    }
  }
}
