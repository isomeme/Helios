package org.onereed.helios.util

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.common.collect.HashMultiset
import timber.log.Timber
import java.util.Locale

class LifecycleLogger  : LifecycleEventObserver {

  override fun onStateChanged(source: LifecycleOwner, event: Event) {
    val tag = getTag(source)
    val state = event.targetState

    val dir = when (event) {
      Event.downTo(state) -> '\u2198' // ↘
      Event.upTo(state) -> '\u2197' // ↗
      else -> '\u219d' // ↝
    }

    Timber.tag(tag).d("%-10s %c %s", event, dir, state)
  }

  companion object {

    private data class SourceId(val className: String, val hashCode: Int) {
      constructor(source: LifecycleOwner) : this(source.javaClass.simpleName, source.hashCode())
    }

    private val classInstanceCounts = HashMultiset.create<String>()
    private val sourceIdTags = mutableMapOf<SourceId, String>()

    fun getTag(source: LifecycleOwner): String {
      val sourceId = SourceId(source)

      return sourceIdTags.getOrPut(sourceId) {
        // add() returns count before insertion, and we want 1-origin.
        val instanceNumber = classInstanceCounts.add(sourceId.className, 1) + 1
        String.format(Locale.US, "LC_%.18s_%d", sourceId.className, instanceNumber)
      }
    }
  }
}
