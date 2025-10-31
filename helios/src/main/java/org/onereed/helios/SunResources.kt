package org.onereed.helios

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8

internal data class SunResources(
  val eventNames: List<String>,
  val fgColors: List<Color>,
  val bgColors: List<Color>,
  val icons: List<Int>,
  val rubrics: List<String>
) {
  companion object {

    fun from(context: Context) : SunResources {
      val resources = context.resources
      val eventNames = resources.getStringArray(R.array.sun_event_names).toList()
      val fgColors = resources.getIntArray(R.array.sun_event_fg_colors).map(::Color)
      val bgColors = resources.getIntArray(R.array.sun_event_bg_colors).map(::Color)
      val icons =
        resources.obtainTypedArray(R.array.sun_event_icons).use { typedArray ->
          (0..3).map { ix -> typedArray.getResourceId(ix, 0) }
        }

      val rubricTemplate = context.readRubricTemplate()
      val slotLists = rubricMadLib.map { resources.getStringArray(it).toList() }

      val rubrics = (0..3).map { event ->
        val subs = slotLists.map { it[event] }.toTypedArray()
        rubricTemplate.format(*subs)
      }

      return SunResources(eventNames, fgColors, bgColors, icons, rubrics)
    }

    private fun Context.readRubricTemplate(): String {
      try {
        return assets.open("rubric_template.md").bufferedReader(UTF_8).use { it.readText() }
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }
    }

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
