package org.onereed.helios;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import org.onereed.helios.databinding.ActivityLiberBinding;
import org.onereed.helios.sun.SunEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityLiberBinding activityLiberBinding = ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(activityLiberBinding.getRoot());
    setSupportActionBar(activityLiberBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    SunEvent sunEvent = checkNotNull(getIntent().getParcelableExtra(Messages.SUN_EVENT_MSG));
    SunEvent.Type type = sunEvent.getType();

    String inocationHtml =
        String.format("file:///android_asset/invocation_%s.html", type.toString().toLowerCase());
    activityLiberBinding.invocation.setBackgroundColor(Color.TRANSPARENT);
    activityLiberBinding.invocation.loadUrl(inocationHtml);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }
}
