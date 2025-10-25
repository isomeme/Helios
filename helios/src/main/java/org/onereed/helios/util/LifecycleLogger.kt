package org.onereed.helios.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

class LifecycleLogger  : LifecycleEventObserver {

  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    val dir = when (event) {
      Lifecycle.Event.downTo(event.targetState) -> "\u2198" // ↘
      Lifecycle.Event.upTo(event.targetState) -> "\u2197" // ↗
      else -> "\u219d" // ↝
    }

    Timber.tag("LCYC_${source.javaClass.simpleName}")
      .d("%-10s %s %s", event, dir, event.targetState)
  }
}
