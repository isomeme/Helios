package org.onereed.helios.compose

/** Navigation routes. */
sealed class Screen(val route: String) {
  object Schedule : Screen("schedule")

  object Text : Screen("text")

  object Compass : Screen("compass")

  object Help : Screen("help")
}
