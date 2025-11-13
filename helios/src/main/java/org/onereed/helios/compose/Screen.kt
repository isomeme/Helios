package org.onereed.helios.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import org.onereed.helios.R

@Serializable
sealed class Screen(@param:StringRes val titleRes: Int, @param:DrawableRes val iconRes: Int) {

  @Serializable
  data object Schedule :
    Screen(R.string.screen_schedule, R.drawable.schedule_24px)

  @Serializable
  data object Text :
    Screen(R.string.screen_text, R.drawable.article_24px)

  @Serializable
  data object Compass :
    Screen(R.string.screen_compass, R.drawable.navigation_24px)

  @Serializable
  data object Settings :
    Screen(R.string.screen_settings, R.drawable.settings_24px)

  companion object {
    val TopLevelScreens = listOf(Schedule, Text, Compass, Settings)
  }
}
