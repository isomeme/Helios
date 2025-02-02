package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onereed.helios.common.ToastUtil.longToastAndFinish;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import org.onereed.helios.common.LogUtil;

/**
 * When activity resumes, checks for Google Play Services availability. If it is not available,
 * tries to get the user to install or upgrade.
 */
class PlayServicesVerifier implements DefaultLifecycleObserver {

  private static final String TAG = LogUtil.makeTag(PlayServicesVerifier.class);

  /** The specific value doesn't matter; the API just needs some int. */
  private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 1;

  private final Activity activity;

  PlayServicesVerifier(Activity activity) {
    this.activity = activity;
  }

  /**
   * Determines whether Google Play Services is available; calls {@link Activity#finish()} on {@link
   * #activity} if not.
   */
  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    var availability = GoogleApiAvailability.getInstance();
    int code = availability.isGooglePlayServicesAvailable(activity);

    if (code != ConnectionResult.SUCCESS) {
      if (availability.isUserResolvableError(code)) {
        Log.w(TAG, "Play Services requires user setup, code=" + code);
        var errorDialog =
            availability.getErrorDialog(
                activity, code, PLAY_SERVICE_RESOLUTION_REQUEST, unused -> activity.finish());
        checkNotNull(errorDialog).show();
      } else {
        Log.e(TAG, "Play Services not available, code=" + code);
        longToastAndFinish(activity, R.string.toast_playservices_unrecoverable);
      }
    }
  }
}
