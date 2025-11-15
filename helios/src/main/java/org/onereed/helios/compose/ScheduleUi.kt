package org.onereed.helios.compose

import android.content.Context
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_NO_YEAR
import android.text.format.DateUtils.FORMAT_NUMERIC_DATE
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.formatDateTime
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.onereed.helios.sun.SunSchedule

data class ScheduleUi(val events: List<EventUi>) {
  data class EventUi(
    val color: Color,
    @param:DrawableRes val iconRes: Int,
    val name: String,
    val timeText: String,
    val isClosestEvent: Boolean,
    val ordinal: Int,
    val key: Long,
  )

  @Singleton
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
            timeText = formatDateTime(context, it.instant.toEpochMilli(), DATE_FORMAT_FLAGS),
            isClosestEvent = it.isClosestEvent,
            ordinal = ordinal,
            key = it.weakId,
          )
        }

      return ScheduleUi(events)
    }

    companion object {

      private const val DATE_FORMAT_FLAGS =
        0 or
          FORMAT_SHOW_DATE or
          FORMAT_NUMERIC_DATE or
          FORMAT_NO_YEAR or
          FORMAT_SHOW_WEEKDAY or
          FORMAT_SHOW_TIME or
          FORMAT_ABBREV_ALL
    }
  }
}
