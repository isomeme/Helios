package org.onereed.helios;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ImmutableList;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.ResourceUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;

class SunEventsAdapter extends RecyclerView.Adapter<SunEventsAdapter.SunEventHolder> {

  private static final String TAG = LogUtil.makeTag(SunEventsAdapter.class);

  private static final int DATE_FORMAT_FLAGS =
      DateUtils.FORMAT_SHOW_DATE
          | DateUtils.FORMAT_NUMERIC_DATE
          | DateUtils.FORMAT_SHOW_WEEKDAY
          | DateUtils.FORMAT_SHOW_TIME
          | DateUtils.FORMAT_ABBREV_ALL;

  static class SunEventHolder extends RecyclerView.ViewHolder {
    private final CardView cardView;
    private final TextView eventTimeView;

    SunEventHolder(CardView cardView) {
      super(cardView);
      this.cardView = cardView;
      this.eventTimeView = cardView.findViewById(R.id.eventTime);
    }
  }

  private final Activity activity;

  private ImmutableList<SunEvent> sunEvents = ImmutableList.of();

  SunEventsAdapter(Activity activity) {
    this.activity = activity;
    setHasStableIds(true);
  }

  void acceptSunEvents(ImmutableList<SunEvent> newSunEvents) {
    AppLogger.debug(TAG, "Accepting newSunEvents=%s", newSunEvents);
    sunEvents = newSunEvents;
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return sunEvents.get(position).getTime().toEpochMilli();
  }

  @NonNull
  @Override
  public SunEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    CardView cardView = (CardView) layoutInflater.inflate(R.layout.sun_event, parent, false);
    return new SunEventHolder(cardView);
  }

  @Override
  public void onBindViewHolder(@NonNull SunEventHolder holder, int position) {
    SunEvent sunEvent = sunEvents.get(position);
    int sunEventTypeOrdinal = sunEvent.getType().ordinal();
    Resources resources = activity.getResources();

    ResourceUtil.withTypedArray(
        resources,
        R.array.sun_event_colors,
        typedArray ->
            holder.cardView.setCardBackgroundColor(typedArray.getColor(sunEventTypeOrdinal, 0)));

    ResourceUtil.withTypedArray(
        resources,
        R.array.sun_event_icons,
        typedArray ->
            holder.eventTimeView.setCompoundDrawablesWithIntrinsicBounds(
                typedArray.getResourceId(sunEventTypeOrdinal, 0), 0, 0, 0));

    long eventTimeMillis = sunEvent.getTime().toEpochMilli();
    String timeStr = DateUtils.formatDateTime(activity, eventTimeMillis, DATE_FORMAT_FLAGS);
    holder.eventTimeView.setText(timeStr);
    holder.eventTimeView.setTypeface(null, sunEvent.isClosest() ? Typeface.BOLD : Typeface.NORMAL);

    holder.cardView.setOnClickListener(ignored -> sendToLiberActivity(sunEventTypeOrdinal));
  }

  private void sendToLiberActivity(int sunEventTypeOrdinal) {
    Intent intent = new Intent(activity, LiberActivity.class);
    intent.putExtra(Messages.SUN_EVENT_TYPE_MSG, sunEventTypeOrdinal);
    activity.startActivity(intent);
  }

  @Override
  public int getItemCount() {
    return sunEvents.size();
  }
}
