package org.onereed.helios.compose

import androidx.compose.runtime.Stable

@Stable
abstract class NavActions {

  abstract fun navigateTo(screen: Screen)

  abstract fun selectTextIndex(index: Int)

  fun navigateToTextIndex(index: Int) {
    selectTextIndex(index)
    navigateTo(Screen.Text)
  }

  companion object {

    /** A do-nothing stub implementation for use in previews and tests. */
    val NavActionsStub =
      object : NavActions() {
        override fun navigateTo(screen: Screen) {}

        override fun selectTextIndex(index: Int) {}
      }
  }
}
