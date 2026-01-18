package org.onereed.helios.datasource

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.core.content.res.use
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import org.onereed.helios.R
import org.onereed.helios.sun.SunEventType

@Immutable
data class SunResources(val eventSets: List<EventSet>) {
  @Immutable
  data class EventSet(
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    val rubric: String,
  )

  companion object {

    fun create(context: Context): SunResources {
      val resources = context.resources

      val names =
        resources.obtainTypedArray(R.array.sun_event_name_ids).use { typedArray ->
          sunEventOrdinals.map { typedArray.getResourceId(it, 0) }
        }

      val icons =
        resources.obtainTypedArray(R.array.sun_event_icons).use { typedArray ->
          sunEventOrdinals.map { typedArray.getResourceId(it, 0) }
        }

      val rubricTemplate = readRubricTemplate(context)
      val slotLists = rubricMadLib.map { resources.getStringArray(it) }

      val rubrics =
        sunEventOrdinals.map { event ->
          val subs = slotLists.map { it[event] }.toTypedArray()
          rubricTemplate.format(*subs)
        }

      val eventSets = sunEventOrdinals.map { EventSet(names[it], icons[it], rubrics[it]) }

      return SunResources(eventSets)
    }

    private fun readRubricTemplate(context: Context): String {
      try {
        return context.assets
          .open("rubric_template.md")
          .bufferedReader(StandardCharsets.UTF_8)
          .use { it.readText() }
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }
    }

    private val sunEventOrdinals = SunEventType.entries.map { it.ordinal }

    private val rubricMadLib =
      listOf(
        R.array.rubric_gods,
        R.array.rubric_gerunds,
        R.array.rubric_nouns,
        R.array.rubric_events,
        R.array.rubric_abodes,
      )
  }
}
