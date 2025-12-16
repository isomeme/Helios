package org.onereed.helios.compose.permission

import android.app.Activity
import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import org.onereed.helios.common.openSettings

@Immutable
@OptIn(ExperimentalPermissionsApi::class)
data class PermissionActions(
  val requestPermission: () -> Unit,
  val openSettings: () -> Unit,
  val exitApp: () -> Unit,
) {
  constructor(
    permissionState: PermissionState,
    activity: Activity?,
  ) : this(
    requestPermission = permissionState::launchPermissionRequest,
    openSettings = { activity?.openSettings() },
    exitApp = { activity?.finishAndRemoveTask() },
  )
}
