package org.onereed.helios;

import android.graphics.Color;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;
import timber.log.Timber;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

  private ActivityLiberBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbar);

    binding.invocation.setWebViewClient(new LocalContentWebViewClient());
    binding.invocation.setBackgroundColor(Color.TRANSPARENT);

    int typeOrdinal =
        getIntent().getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal());
    displayInvocation(typeOrdinal);

    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            this, R.array.sun_event_names, android.R.layout.simple_spinner_item);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    binding.sunEventSelector.setAdapter(adapter);
    binding.sunEventSelector.setSelection(typeOrdinal);
    binding.sunEventSelector.setOnItemSelectedListener(this);
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
    SunEvent.Type type = SunEvent.Type.values()[sunEventTypeOrdinal];
    String invocationHtml =
        String.format(
            "https://appassets.androidplatform.net/assets/invocation_%s.html",
            type.toString().toLowerCase());

    binding.invocation.loadUrl(invocationHtml);
  }

  private class LocalContentWebViewClient extends WebViewClientCompat {

    private final WebViewAssetLoader assetLoader =
        new WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/", new WebViewAssetLoader.AssetsPathHandler(LiberActivity.this))
            .addPathHandler(
                "/res/", new WebViewAssetLoader.ResourcesPathHandler(LiberActivity.this))
            .build();

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
      return assetLoader.shouldInterceptRequest(request.getUrl());
    }
  }
}
