package org.onereed.helios.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import org.onereed.helios.R

@Serializable
sealed class Screen(@param:StringRes val titleRes: Int, @param:DrawableRes val iconRes: Int) {

  @Serializable
  data object Schedule :
    Screen(R.string.screen_schedule, R.drawable.schedule_24dp_e3e3e3_fill0_wght400_grad0_opsz24)

  @Serializable
  data object Text :
    Screen(R.string.screen_text, R.drawable.article_24dp_e3e3e3_fill0_wght400_grad0_opsz24)

  @Serializable
  data object Compass :
    Screen(R.string.screen_compass, R.drawable.navigation_24dp_e3e3e3_fill0_wght400_grad0_opsz24)

  @Serializable
  data object Help :
    Screen(R.string.screen_help, R.drawable.help_24dp_e3e3e3_fill0_wght400_grad0_opsz24)

  companion object {
    val TopLevelScreens = listOf(Schedule, Text, Compass, Help)
  }
}
