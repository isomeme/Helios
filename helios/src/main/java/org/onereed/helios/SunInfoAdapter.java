package org.onereed.helios;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.onereed.helios.common.TypedArrayAccessor;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

class SunInfoAdapter extends RecyclerView.Adapter<SunInfoAdapter.SunEventViewHolder> {

  private static final int DATE_FORMAT_FLAGS =
      DateUtils.FORMAT_SHOW_DATE
          | DateUtils.FORMAT_NUMERIC_DATE
          | DateUtils.FORMAT_SHOW_WEEKDAY
          | DateUtils.FORMAT_SHOW_TIME
          | DateUtils.FORMAT_ABBREV_ALL;

  static class SunEventViewHolder extends RecyclerView.ViewHolder {
    private final CardView cardView;
    private final TextView eventTimeView;

    SunEventViewHolder(CardView cardView) {
      super(cardView);
      this.cardView = cardView;
      this.eventTimeView = cardView.findViewById(R.id.eventTime);
    }
  }

  private SunInfo sunInfo = null;

  SunInfoAdapter() {
    setHasStableIds(true);
  }

  void acceptSunInfo(SunInfo newSunInfo) {
    sunInfo = newSunInfo;
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return getSunEvent(position).getWeakId();
  }

  @NonNull
  @Override
  public SunEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    CardView cardView = (CardView) layoutInflater.inflate(R.layout.sun_event, parent, false);
    return new SunEventViewHolder(cardView);
  }

  @Override
  public void onBindViewHolder(@NonNull SunEventViewHolder sunEventViewHolder, int position) {
    SunEvent sunEvent = getSunEvent(position);
    int typeOrdinal = sunEvent.getType().ordinal();
    Context context = sunEventViewHolder.itemView.getContext();
    TypedArrayAccessor typedArrayAccessor = TypedArrayAccessor.create(context, typeOrdinal);

    int bgColor = typedArrayAccessor.getColor(R.array.sun_event_bg_colors);
    sunEventViewHolder.cardView.setCardBackgroundColor(bgColor);

    int iconId = typedArrayAccessor.getResourceId(R.array.sun_event_icons);
    sunEventViewHolder.eventTimeView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);

    long eventTimeMillis = sunEvent.getTime().toEpochMilli();
    String timeText = DateUtils.formatDateTime(context, eventTimeMillis, DATE_FORMAT_FLAGS);
    int timeStyle = position == sunInfo.getClosestEventIndex() ? Typeface.BOLD : Typeface.NORMAL;
    sunEventViewHolder.eventTimeView.setText(timeText);
    sunEventViewHolder.eventTimeView.setTypeface(/* tf= */ null, timeStyle);

    sunEventViewHolder.cardView.setOnClickListener(
        view -> sendToLiberActivity(view.getContext(), typeOrdinal));
  }

  @Override
  public int getItemCount() {
    return sunInfo == null ? 0 : sunInfo.getSunEvents().size();
  }

  private SunEvent getSunEvent(int position) {
    return sunInfo.getSunEvents().get(position);
  }

  private static void sendToLiberActivity(Context context, int typeOrdinal) {
    Intent intent = new Intent(context, LiberActivity.class);
    intent.putExtra(IntentExtraTags.SUN_EVENT_TYPE, typeOrdinal);
    context.startActivity(intent);
  }
}
