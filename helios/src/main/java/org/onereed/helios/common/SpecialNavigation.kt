package org.onereed.helios.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Activity.openSettings() {
  val appUri = Uri.fromParts("package", packageName, /* fragment= */ null)
  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(appUri)
  startActivity(intent)
}
