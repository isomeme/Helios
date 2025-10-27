package org.onereed.helios

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions.makeSceneTransitionAnimation
import android.content.Intent
import android.graphics.Typeface
import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_NUMERIC_DATE
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.formatDateTime
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.onereed.helios.SunScheduleAdapter.EventViewHolder
import org.onereed.helios.sun.SunSchedule

internal class SunScheduleAdapter(private val activity: Activity) :
  RecyclerView.Adapter<EventViewHolder>() {

  private var sunSchedule: SunSchedule? = null

  init {
    setHasStableIds(true)
  }

  @SuppressLint("NotifyDataSetChanged")
  fun acceptSunSchedule(value: SunSchedule) {
    sunSchedule = value
    notifyDataSetChanged()
  }

  override fun getItemCount() = sunSchedule?.events?.size ?: 0

  override fun getItemId(position: Int) = sunSchedule!!.events[position].weakId

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val cardView =
      layoutInflater.inflate(R.layout.sun_event, parent, /* attachToRoot= */ false) as CardView
    return EventViewHolder(cardView)
  }

  override fun onBindViewHolder(sunEventViewHolder: EventViewHolder, position: Int) {
    val event = sunSchedule!!.events[position]
    val typeOrdinal = event.sunEventType.ordinal

    activity.resources.apply {
      obtainTypedArray(R.array.sun_event_bg_colors).use { typedArray ->
        val bgColor = typedArray.getColor(typeOrdinal, /* defValue= */ 0)
        sunEventViewHolder.cardView.setCardBackgroundColor(bgColor)
      }

      obtainTypedArray(R.array.sun_event_icons).use { typedArray ->
        val iconId = typedArray.getResourceId(typeOrdinal, /* defValue= */ 0)
        sunEventViewHolder.eventTimeView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)
      }
    }

    val eventTimeMillis = event.instant.toEpochMilli()
    val timeText = formatDateTime(activity, eventTimeMillis, DATE_FORMAT_FLAGS)
    val timeStyle = if (event.isClosestEvent) Typeface.BOLD else Typeface.NORMAL

    sunEventViewHolder.eventTimeView.apply {
      text = timeText
      setTypeface(/* tf= */ null, timeStyle)
    }

    sunEventViewHolder.cardView.setOnClickListener { view ->
      sendToTextActivity(view, typeOrdinal)
    }
  }

  private fun sendToTextActivity(view: View, typeOrdinal: Int) {
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    val intent =
      Intent(activity, TextActivity::class.java)
        .putExtra(TextActivity.SUN_EVENT_TYPE_ORDINAL, typeOrdinal)
    val bundle = makeSceneTransitionAnimation(activity).toBundle()
    activity.startActivity(intent, bundle)
  }

  internal data class EventViewHolder(
    val cardView: CardView,
    val eventTimeView: TextView = cardView.findViewById(R.id.eventTime),
  ) : RecyclerView.ViewHolder(cardView)

  companion object {

    private const val DATE_FORMAT_FLAGS =
      0 or
        FORMAT_SHOW_DATE or
        FORMAT_NUMERIC_DATE or
        FORMAT_SHOW_WEEKDAY or
        FORMAT_SHOW_TIME or
        FORMAT_ABBREV_ALL
  }
}
