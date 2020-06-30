package org.onereed.helios;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.common.collect.ImmutableMap;

import org.onereed.helios.common.TypedArrayUtil;
import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends AbstractMenuActivity {

  private ActivityLiberBinding activityLiberBinding;

  private String[] sunEventNames = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activityLiberBinding = ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(activityLiberBinding.getRoot());
    setSupportActionBar(activityLiberBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    activityLiberBinding.invocation.setBackgroundColor(Color.TRANSPARENT);

    int sunEventTypeOrdinal = getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, -1);
    displayInvocation(sunEventTypeOrdinal);

    sunEventNames = TypedArrayUtil.getStringArray(this, R.array.sun_event_names);

    activityLiberBinding.buttonTextSelect.setOnClickListener(this::textSelectDialog);
  }

  @Override
  protected Map<Integer, Runnable> getMenuActions() {
    return ImmutableMap.of(
        R.id.action_direction, () -> startActivity(new Intent(this, CompassActivity.class)));
  }

  private void textSelectDialog(View unused) {
    new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_select_text)
        .setItems(sunEventNames, (dialog, which) -> displayInvocation(which))
        .show();
  }

  private void displayInvocation(int sunEventTypeOrdinal) {
    SunEvent.Type type = SunEvent.Type.values()[sunEventTypeOrdinal];
    String invocationHtml =
        String.format("file:///android_asset/invocation_%s.html", type.toString().toLowerCase());
    activityLiberBinding.invocation.loadUrl(invocationHtml);
  }
}
