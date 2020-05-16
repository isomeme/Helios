package org.onereed.helios;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

class SunInfoAdapter extends RecyclerView.Adapter<SunInfoAdapter.SunEventHolder> {

  private static final String TAG = LogUtil.makeTag(SunInfoAdapter.class);

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

  private SunInfo sunInfo = null;

  SunInfoAdapter(Activity activity) {
    this.activity = activity;
  }

  void acceptSunInfo(SunInfo newSunInfo) {
    AppLogger.debug(TAG, "Accepting newSunInfo=%s", newSunInfo);
      sunInfo = newSunInfo;
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
    SunEvent sunEvent = sunInfo.getSunEvents().get(position);
    boolean isClosestEvent = sunInfo.getIndexOfClosestEvent() == position;

    int colorResource = sunEvent.getType().getColorResource();
    int color = activity.getResources().getColor(colorResource, null);
    holder.cardView.setCardBackgroundColor(color);

    long eventTimeMillis = sunEvent.getTime().toEpochMilli();
    String timeStr =
        DateUtils.formatDateTime(
            activity,
            eventTimeMillis,
            DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NUMERIC_DATE
                | DateUtils.FORMAT_SHOW_TIME);
    holder.eventTimeView.setText(timeStr);
    holder.eventTimeView.setTypeface(null, isClosestEvent ? Typeface.BOLD : Typeface.NORMAL);
  }

  @Override
  public int getItemCount() {
    return sunInfo == null ? 0 : sunInfo.getSunEvents().size();
  }
}
