package org.onereed.helios

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8
import org.onereed.helios.sun.SunEventType

internal data class SunResources(
  val eventSets: List<EventSet>,
) {
  data class EventSet(
    val name: String,
    val fgColor: Color,
    val bgColor: Color,
    val icon: Int,
    val rubric: String,
  )

  companion object {

    fun from(context: Context): SunResources {
      val resources = context.resources
      val names = resources.getStringArray(R.array.sun_event_names).toList()
      val fgColors = resources.getIntArray(R.array.sun_event_fg_colors).map(::Color)
      val bgColors = resources.getIntArray(R.array.sun_event_bg_colors).map(::Color)
      val icons =
        resources.obtainTypedArray(R.array.sun_event_icons).use { typedArray ->
          sunEventOrdinals.map { typedArray.getResourceId(it, 0) }
        }

      val rubricTemplate = context.readRubricTemplate()
      val slotLists = rubricMadLib.map { resources.getStringArray(it) }

      val rubrics =
        sunEventOrdinals.map { event ->
          val subs = slotLists.map { it[event] }.toTypedArray()
          rubricTemplate.format(*subs)
        }

      val eventSets = sunEventOrdinals.map {
        EventSet(names[it], fgColors[it], bgColors[it], icons[it], rubrics[it])
      }

      return SunResources(eventSets)
    }

    private fun Context.readRubricTemplate(): String {
      try {
        return assets.open("rubric_template.md").bufferedReader(UTF_8).use { it.readText() }
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
