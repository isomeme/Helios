package org.onereed.helios.compose

import android.content.Context
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_NUMERIC_DATE
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.formatDateTime
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.onereed.helios.sun.SunSchedule

data class ScheduleUi(val events: List<EventUi>) {
  data class EventUi(
    val color: Color,
    @param:DrawableRes val iconRes: Int,
    val name: String,
    val timeText: String,
    val timeFontWeight: FontWeight,
    val ordinal: Int,
    val key: Long,
  )

  companion object {

    fun create(context: Context, sunSchedule: SunSchedule, sunResources: SunResources): ScheduleUi {
      val events =
        sunSchedule.events.map {
          val ordinal = it.sunEventType.ordinal
          val eventSet = sunResources.eventSets[ordinal]

          EventUi(
            color = eventSet.fgColor,
            iconRes = eventSet.iconRes,
            name = eventSet.name,
            timeText = formatDateTime(context, it.instant.toEpochMilli(), DATE_FORMAT_FLAGS),
            timeFontWeight = if (it.isClosestEvent) FontWeight.Bold else FontWeight.Normal,
            ordinal = ordinal,
            key = it.weakId,
          )
        }

      return ScheduleUi(events)
    }

    private const val DATE_FORMAT_FLAGS =
      0 or
        FORMAT_SHOW_DATE or
        FORMAT_NUMERIC_DATE or
        FORMAT_SHOW_WEEKDAY or
        FORMAT_SHOW_TIME or
        FORMAT_ABBREV_ALL
  }
}
