package org.onereed.helios;

import static org.onereed.helios.common.AssetUtil.readAssetText;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import io.noties.markwon.Markwon;
import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;
import timber.log.Timber;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

  private ActivityLiberBinding binding;

  private Markwon markwon;

  private String invocationTemplate;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate start");
    super.onCreate(savedInstanceState);

    binding = ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbar);

    String adoration = readAssetText(this, "adoration.md");
    invocationTemplate = readAssetText(this, "invocation_template.md");

    markwon = Markwon.create(this);
    markwon.setMarkdown(binding.adorationDisplay, adoration);

    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            this, R.array.sun_event_names, android.R.layout.simple_spinner_item);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.sunEventSelector.setAdapter(adapter);

    int typeOrdinal =
        getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal());

    // The animate=false argument tells the item selected listener not to run based on this
    // initial selection. This avoids a double haptic click, one from the activity transition and
    // one from onItemSelected. Instead, we call displayInvocation directly for the initial load.

    binding.sunEventSelector.setSelection(typeOrdinal, /* animate= */ false);
    binding.sunEventSelector.setOnItemSelectedListener(this);
    displayInvocation(typeOrdinal);

    Timber.d("onCreate end");
  }

  @Override
  protected int myActionsMenuId() {
    return R.id.action_text;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    Timber.d("onItemSelected: position=%d", position);
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
    displayInvocation(position);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    Timber.d("onNothingSelected");
  }

  private void displayInvocation(int ix) {
    Timber.d("displayInvocation: ix=%d", ix);
    Resources res = getResources();

    try (TypedArray icons = res.obtainTypedArray(R.array.sun_event_icons)) {
      Drawable icon = icons.getDrawable(ix);
      binding.iconDisplay.setImageDrawable(icon);
    }

    String title = res.getStringArray(R.array.sun_event_names)[ix];
    binding.titleDisplay.setText(title);

    int color = res.getIntArray(R.array.sun_event_fg_colors)[ix];
    binding.iconDisplay.setColorFilter(color);
    binding.titleDisplay.setTextColor(color);

    String god = res.getStringArray(R.array.invocation_gods)[ix];
    String gerund = res.getStringArray(R.array.invocation_gerunds)[ix];
    String noun = res.getStringArray(R.array.invocation_nouns)[ix];
    String event = res.getStringArray(R.array.invocation_events)[ix];
    String abode = res.getStringArray(R.array.invocation_abodes)[ix];

    String invocation = String.format(invocationTemplate, god, gerund, noun, event, abode);
    markwon.setMarkdown(binding.invocationDisplay, invocation);

    binding.scrollView.fullScroll(View.FOCUS_UP);
  }
}
