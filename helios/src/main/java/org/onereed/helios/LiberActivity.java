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
    Timber.d("onCreate");
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

    int typeOrdinal =
        getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal());

    binding.sunEventSelector.setAdapter(adapter);
    binding.sunEventSelector.setOnItemSelectedListener(this);
    binding.sunEventSelector.setSelection(typeOrdinal);
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
