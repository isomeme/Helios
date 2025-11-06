package org.onereed.helios.compose

import org.onereed.helios.R

enum class AppDestination(val label: String, val iconId: Int) {
  SCHEDULE("Schedule", R.drawable.schedule_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  TEXT("Text", R.drawable.article_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  COMPASS("Compass", R.drawable.navigation_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  HELP("Help", R.drawable.help_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
}
