package org.onereed.helios.compose

import org.onereed.helios.R

enum class AppDestination(val label: String, val iconId: Int, val route: String) {
  SCHEDULE(
    "Schedule",
    R.drawable.schedule_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
    Screen.Schedule.route,
  ),
  TEXT("Text", R.drawable.article_24dp_e3e3e3_fill0_wght400_grad0_opsz24, Screen.Text.route),
  COMPASS(
    "Compass",
    R.drawable.navigation_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
    Screen.Compass.route,
  ),
  HELP("Help", R.drawable.help_24dp_e3e3e3_fill0_wght400_grad0_opsz24, Screen.Help.route),
}
