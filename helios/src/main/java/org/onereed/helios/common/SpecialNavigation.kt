package org.onereed.helios.common

import android.app.Activity
import org.onereed.shared.settingsIntent

fun Activity.openSettings() {
  startActivity(settingsIntent())
}
