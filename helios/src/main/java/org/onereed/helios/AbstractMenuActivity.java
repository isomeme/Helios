package org.onereed.helios;

import static org.onereed.helios.common.ToastUtil.longToast;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import com.google.errorprone.annotations.ForOverride;
import java.util.Set;

/** Parent class for activities sharing the common Helios menu. */
abstract class AbstractMenuActivity extends AppCompatActivity {

  private static final String HELP_PAGE = "https://www.one-reed.org/helios";
  private static final Intent HELP_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse(HELP_PAGE));

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);

    getOptionsMenuItems().stream()
        .map(menu::findItem)
        .forEach(menuItem -> menuItem.setVisible(true));

    return super.onCreateOptionsMenu(menu);
  }

  @Keep // Called via onClick
  public final void openHelp(MenuItem unused) {
    try {
      startActivity(HELP_INTENT);
    } catch (ActivityNotFoundException e) {
      longToast(this, R.string.toast_no_browser);
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

  /**
   * Subclass implementations of this method are called to determine what options menu items should
   * be visible. The 'Help' menu item is always visible, so it need not be included in this set.
   */
  @ForOverride
  protected abstract Set<Integer> getOptionsMenuItems();
}
