package org.onereed.helios;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import com.google.errorprone.annotations.ForOverride;
import java.util.Set;

/** Parent class for Helios activities. */
abstract class BaseActivity extends AppCompatActivity {

  private static final String HELP_PAGE = "https://www.one-reed.org/helios";
  private static final Intent HELP_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse(HELP_PAGE));

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actions_menu, menu);

    getActionsMenuItems().stream()
        .map(menu::findItem)
        .forEach(menuItem -> menuItem.setVisible(true));

    return super.onCreateOptionsMenu(menu);
  }

  /**
   * Subclass implementations of this method are called to determine what actions should be visible
   * in the top bar (or its spillover menu). The 'Help' action is always visible, so it need not be
   * included in this set.
   */
  @ForOverride
  protected abstract Set<Integer> getActionsMenuItems();

  @Keep // Called via onClick
  public final void openHelp(MenuItem unused) {
    try {
      startActivity(HELP_INTENT);
    } catch (ActivityNotFoundException e) {
      runOnUiThread(
          () ->
              Toast.makeText(this, getString(R.string.toast_no_browser), Toast.LENGTH_LONG).show());
    }
  }

  @Keep // Called via onClick
  public final void openCompass(MenuItem unused) {
    startActivity(new Intent(this, CompassActivity.class));
  }

  @Keep // Called via onClick
  public final void openLiber(MenuItem unused) {
    startActivity(new Intent(this, LiberActivity.class));
  }
}
