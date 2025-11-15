package org.onereed.helios.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

interface PermissionActions {

  fun requestPermission()

  fun openSettings()

  fun exitApp()

  companion object {

    @OptIn(ExperimentalPermissionsApi::class)
    fun create(permissionState: PermissionState, activity: Activity?): PermissionActions =
      object : PermissionActions {

        override fun requestPermission() {
          permissionState.launchPermissionRequest()
        }

        override fun openSettings() {
          activity?.let { act ->
            val intent =
              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", act.packageName, /* fragment= */ null)
              }
            act.startActivity(intent)
          }
        }

        override fun exitApp() {
          activity?.finishAndRemoveTask()
        }
      }
  }
}
