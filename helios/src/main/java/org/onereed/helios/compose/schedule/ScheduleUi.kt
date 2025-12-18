package org.onereed.helios.compose.schedule

import android.content.Context
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_NO_YEAR
import android.text.format.DateUtils.FORMAT_NUMERIC_DATE
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.formatDateTime
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.sun.SunSchedule

@OptIn(ExperimentalTime::class)
@Stable
data class ScheduleUi(val events: List<EventUi>) {
  @Stable
  data class EventUi(
    val color: Color,
    @param:DrawableRes val iconRes: Int,
    val name: String,
    val timeText: String,
    val isClosestEvent: Boolean,
    val ordinal: Int,
    val key: Long,
  )

  class Factory
  @Inject
  constructor(
    @param:ApplicationContext private val context: Context,
    private val sunResources: SunResources,
  ) {

    fun create(sunSchedule: SunSchedule): ScheduleUi {
      val events =
        sunSchedule.events.map {
          val ordinal = it.sunEventType.ordinal
          val eventSet = sunResources.eventSets[ordinal]

          EventUi(
            color = eventSet.color,
            iconRes = eventSet.iconRes,
            name = eventSet.name,
            timeText = formatInstant(context, it.instant),
            isClosestEvent = it.isClosestEvent,
            ordinal = ordinal,
            key = it.weakId,
          )
        }

      return ScheduleUi(events)
    }

    companion object {

      /**
       * Because the time is the most important part of the schedule date-time display, we place it
       * first in the string. This both makes it more prominent and protects it from truncation if
       * the text overflows the available space.
       */
      fun formatInstant(context: Context, instant: Instant): String {
        val millis = instant.toEpochMilliseconds()
        val timePart = formatDateTime(context, millis, TIME_FORMAT_FLAGS)
        val datePart = formatDateTime(context, millis, DATE_FORMAT_FLAGS)
        return "$timePart $datePart"
      }

      private const val DATE_FORMAT_FLAGS =
        0 or
          FORMAT_SHOW_DATE or
          FORMAT_NUMERIC_DATE or
          FORMAT_NO_YEAR or
          FORMAT_SHOW_WEEKDAY or
          FORMAT_ABBREV_ALL

      private const val TIME_FORMAT_FLAGS = 0 or FORMAT_SHOW_TIME or FORMAT_ABBREV_ALL
    }
  }
}
