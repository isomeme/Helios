package org.onereed.helios.compose

interface NavActions {

  fun navigateTo(screen: Screen)

  fun selectTextIndex(index: Int)

  fun navigateToTextIndex(index: Int) {
    selectTextIndex(index)
    navigateTo(Screen.Text)
  }

  companion object {

    fun create(
      heliosAppState: HeliosAppState,
      heliosAppViewModel: HeliosAppViewModel,
    ): NavActions =
      object : NavActions {
        override fun navigateTo(screen: Screen) = heliosAppState.navigateTo(screen)

        override fun selectTextIndex(index: Int) = heliosAppViewModel.selectTextIndex(index)
      }

    /** A do-nothing stub implementation for use in previews and tests. */
    val NavActionsStub =
      object : NavActions {
        override fun navigateTo(screen: Screen) {}

        override fun selectTextIndex(index: Int) {}
      }
  }
}
