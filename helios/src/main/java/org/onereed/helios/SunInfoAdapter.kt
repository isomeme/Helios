package org.onereed.helios

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
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
import kotlinx.coroutines.flow.FlowCollector
import org.onereed.helios.SunInfoAdapter.SunEventViewHolder
import org.onereed.helios.sun.SunInfo

internal class SunInfoAdapter(private val activity: Activity) :
  RecyclerView.Adapter<SunEventViewHolder>(), FlowCollector<SunInfo?> {

  private var sunInfo: SunInfo? = null

  init {
    setHasStableIds(true)
  }

  @SuppressLint("NotifyDataSetChanged")
  override suspend fun emit(value: SunInfo?) {
    sunInfo = value ?: return
    notifyDataSetChanged()
  }

  override fun getItemCount() = sunInfo?.sunEvents?.size ?: 0

  override fun getItemId(position: Int) = sunInfo!!.sunEvents[position].weakId

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SunEventViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val cardView =
      layoutInflater.inflate(R.layout.sun_event, parent, /* attachToRoot= */ false) as CardView
    return SunEventViewHolder(cardView)
  }

  override fun onBindViewHolder(sunEventViewHolder: SunEventViewHolder, position: Int) {
    val sunEvent = sunInfo!!.sunEvents[position]
    val typeOrdinal = sunEvent.type.ordinal

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

    val eventTimeMillis = sunEvent.instant.toEpochMilli()
    val timeText = formatDateTime(activity, eventTimeMillis, DATE_FORMAT_FLAGS)
    val timeStyle = if (position == sunInfo!!.closestEventIndex) Typeface.BOLD else Typeface.NORMAL

    sunEventViewHolder.eventTimeView.apply {
      text = timeText
      setTypeface(/* tf= */ null, timeStyle)
    }

    sunEventViewHolder.cardView.setOnClickListener { view ->
      sendToLiberActivity(view, typeOrdinal)
    }
  }

  private fun sendToLiberActivity(view: View, typeOrdinal: Int) {
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    val intent = Intent(activity, LiberActivity::class.java)
    intent.putExtra(IntentExtraTags.SUN_EVENT_TYPE, typeOrdinal)
    val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
    activity.startActivity(intent, bundle)
  }

  internal data class SunEventViewHolder(
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
