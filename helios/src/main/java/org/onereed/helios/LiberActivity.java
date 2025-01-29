package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;

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

    int typeOrdinal =
        getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal());
    displayInvocation(typeOrdinal);

    sunEventNames = getResources().getStringArray(R.array.sun_event_names);
    activityLiberBinding.buttonTextSelect.setOnClickListener(this::textSelectDialog);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_direction);
  }

  private void textSelectDialog(View unused) {
    new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_select_text)
        .setItems(sunEventNames, (dialog, which) -> displayInvocation(which))
        .show();
  }

  private void displayInvocation(int sunEventTypeOrdinal) {
    var type = SunEvent.Type.values()[sunEventTypeOrdinal];
    String invocationHtml =
        String.format("file:///android_asset/invocation_%s.html", type.toString().toLowerCase());
    activityLiberBinding.invocation.loadUrl(invocationHtml);
  }
}
