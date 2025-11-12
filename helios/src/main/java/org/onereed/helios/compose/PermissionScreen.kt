package org.onereed.helios.compose

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(locationPermissionState: PermissionState) {
  val context = LocalContext.current

  // Track if the permission request has been processed after user interaction
  var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
  var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(locationPermissionState.status) {
    // Check if the permission state has changed after the request
    if (hasRequestedPermission) {
      permissionRequestCompleted = true
    }
  }

  Box(
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.wrapContentSize().padding(30.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (permissionRequestCompleted) {
        // Show rationale only after the permission request is completed
        if (locationPermissionState.status.shouldShowRationale) {
          Text(
            "Location permission is required to use this feature.",
            color = MaterialTheme.colorScheme.onSurface,
          )
          Button(
            onClick = {
              locationPermissionState.launchPermissionRequest()
              hasRequestedPermission = true
            }
          ) {
            Text("Request location permission")
          }
        } else {
          // Show "Denied" message only after the user has denied permission
          Text(
            "Location permission denied. Please enable it in the app settings to proceed.",
            color = MaterialTheme.colorScheme.onSurface,
          )
          Button(
            onClick = {
              // Open app settings to manually enable the permission
              val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                  data = Uri.fromParts("package", context.packageName, /* fragment= */ null)
                }
              context.startActivity(intent)
            }
          ) {
            Text("Open app settings")
          }
        }
      } else {
        // Show the initial request button
        Button(
          onClick = {
            locationPermissionState.launchPermissionRequest()
            hasRequestedPermission = true
          }
        ) {
          Text("Request location permission")
        }
      }
    }
  }
}
