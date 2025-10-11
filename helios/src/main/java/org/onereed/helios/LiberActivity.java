package org.onereed.helios;

import static org.onereed.helios.common.AssetUtil.readAssetText;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import java.util.Set;
import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;
import timber.log.Timber;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

  private ActivityLiberBinding binding;

  private Markwon markwon;

  private ImmutableList<String> invocationAssetNames;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    binding = ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbar);

    markwon = Markwon.builder(this).usePlugin(HtmlPlugin.create()).build();

    String adorationText = readAssetText(this, "adoration.md");
    markwon.setMarkdown(binding.adorationDisplay, adorationText);

    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            this, R.array.sun_event_names, android.R.layout.simple_spinner_item);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    int typeOrdinal =
        getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal());

    binding.sunEventSelector.setAdapter(adapter);
    binding.sunEventSelector.setOnItemSelectedListener(this);
    binding.sunEventSelector.setSelection(typeOrdinal);

    invocationAssetNames =
        ImmutableList.copyOf(getResources().getStringArray(R.array.invocation_asset_names));
  }

  @Override
  protected Set<Integer> getActionsMenuItems() {
    return ImmutableSet.of(R.id.action_compass);
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

  private void displayInvocation(int sunEventTypeOrdinal) {
    String invocationText = readAssetText(this, invocationAssetNames.get(sunEventTypeOrdinal));
    markwon.setMarkdown(binding.invocationDisplay, invocationText);

    binding.scrollView.fullScroll(View.FOCUS_UP);
  }
}
