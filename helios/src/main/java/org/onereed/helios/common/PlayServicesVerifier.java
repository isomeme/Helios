package org.onereed.helios.common;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.onereed.helios.R;

/**
 * When activity resumes, checks for Google Play Services availability. If it is not available,
 * tries to get the user to install or upgrade.
 */
public class PlayServicesVerifier implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(PlayServicesVerifier.class);

  /** The specific value doesn't matter; the API just needs some int. */
  private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 1;

  private final Activity activity;

  public PlayServicesVerifier(Activity activity) {
    this.activity = activity;
  }

  /**
   * Determines whether Google Play Services is available; calls {@link Activity#finish()} on {@link
   * #activity} if not.
   */
  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
    int code = googleApiAvailability.isGooglePlayServicesAvailable(activity);

    if (code != ConnectionResult.SUCCESS) {
      if (googleApiAvailability.isUserResolvableError(code)) {
        Log.w(TAG, "Play Services requires user setup, code=" + code);
        googleApiAvailability
            .getErrorDialog(
                activity, code, PLAY_SERVICE_RESOLUTION_REQUEST, unused -> activity.finish())
            .show();
      } else {
        Log.e(TAG, "Play Services not available, code=" + code);
        ToastUtil.longToastAndFinish(activity, R.string.toast_playservices_unrecoverable);
      }
    }
  }
}
