package org.onereed.helios.compose

import androidx.compose.runtime.Stable

@Stable
interface NavActions {

  fun navigateTo(screen: Screen)

  fun selectTextIndex(index: Int)

  fun navigateToTextIndex(index: Int) {
    selectTextIndex(index)
    navigateTo(Screen.Text)
  }

  companion object {

    fun create(heliosAppState: HeliosAppState, heliosAppViewModel: HeliosAppViewModel): NavActions =
      object : NavActions {
        override fun navigateTo(screen: Screen) = heliosAppState.navigateTo(screen)

        override fun selectTextIndex(index: Int) = heliosAppViewModel.selectTextIndex(index)
      }
  }
}
