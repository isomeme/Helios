package org.onereed.helios.compose.compass

/** Z-index values for compass screen display elements, ordered from the back forward. */
enum class ZIndex {

  VIEW_LINE,
  COMPASS_FACE,
  SUN_AND_ARROW,
  SUN_EVENT,
  OVERLAY;

  val zIndex: Float = ordinal.toFloat()
}
