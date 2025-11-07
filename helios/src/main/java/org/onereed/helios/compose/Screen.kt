package org.onereed.helios.compose

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

  @Serializable
  object Schedule : Screen()

  @Serializable
  data class Text(val selectedIndex : Int? = null) : Screen()

  @Serializable
  object Compass : Screen()

  @Serializable
  object Help : Screen()
}
