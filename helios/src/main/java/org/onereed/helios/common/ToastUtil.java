package org.onereed.helios.common;

import android.app.Activity;
import android.widget.Toast;

/** Static utilities for making Toast appear. */
public class ToastUtil {

  public static void longToast(Activity activity, int stringId) {
    activity.runOnUiThread(
        () -> Toast.makeText(activity, activity.getString(stringId), Toast.LENGTH_LONG).show());
  }

  static void longToastAndFinish(Activity activity, int stringId) {
    longToast(activity, stringId);
    activity.finish();
  }

  private ToastUtil() {}
}
