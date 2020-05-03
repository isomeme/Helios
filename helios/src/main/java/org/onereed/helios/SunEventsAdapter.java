package org.onereed.helios;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ImmutableMap;

import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class SunEventsAdapter extends RecyclerView.Adapter<SunEventsAdapter.SunEventHolder> {

  private static final ImmutableMap<SunEvent.Type, Integer> EVENT_COLOR_RESOURCES =
      ImmutableMap.of(
          SunEvent.Type.RISE, R.color.bg_rise,
          SunEvent.Type.NOON, R.color.bg_noon,
          SunEvent.Type.SET, R.color.bg_set,
          SunEvent.Type.NADIR, R.color.bg_nadir);

  static class SunEventHolder extends RecyclerView.ViewHolder {
    private CardView cardView;
    private TextView textView;

    SunEventHolder(CardView cardView) {
      super(cardView);
      this.cardView = cardView;
      textView = itemView.findViewById(R.id.eventTime);
    }
  }

  private final Activity activity;
  private final List<SunEvent> sunEvents = new ArrayList<>();

  SunEventsAdapter(Activity activity) {
    this.activity = activity;
  }

  void acceptSunInfo(SunInfo sunInfo) {
    sunEvents.clear();
    sunEvents.addAll(sunInfo.getSunEvents());
    notifyDataSetChanged();
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

    int colorResource = checkNotNull(EVENT_COLOR_RESOURCES.get(sunEvent.getType()));
    int color = activity.getResources().getColor(colorResource, null);
    holder.cardView.setCardBackgroundColor(color);

    long eventTimeMillis = sunEvent.getTime().toEpochMilli();
    String timeStr =
        DateUtils.formatDateTime(activity, eventTimeMillis,
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME);
    holder.textView.setText(timeStr);
  }

  @Override
  public int getItemCount() {
    return sunEvents.size();
  }
}
