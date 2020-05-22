package org.onereed.helios;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import org.onereed.helios.databinding.ActivityLiberBinding;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/** Activity for displaying the text of Liber Resh. */
public class LiberActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityLiberBinding activityLiberBinding =
        ActivityLiberBinding.inflate(getLayoutInflater());
    setContentView(activityLiberBinding.getRoot());
    setSupportActionBar(activityLiberBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    Intent intent = getIntent();
    int ordinal = intent.getIntExtra(Messages.SUN_EVENT_MSG, 0);
    String eventStr = getResources().getStringArray(R.array.sun_event_names)[ordinal];
    activityLiberBinding.header.setText(String.format(Locale.ENGLISH, "%s Resh", eventStr));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }
}
