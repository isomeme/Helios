package org.onereed.helios;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.ForOverride;

import java.util.Set;

/** Parent class for activities sharing the common Helios menu. */
abstract class AbstractMenuActivity extends AppCompatActivity {

  private static final ImmutableSet<Integer> MENU_ITEM_IDS =
      ImmutableSet.of(
          R.id.action_direction,
          R.id.action_refresh,
          R.id.action_text,
          R.id.action_help);

  public static final Uri HELP_URI = Uri.parse("https://www.one-reed.org/helios");

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);

    getOptionsMenuItems().stream()
        .map(menu::findItem)
        .forEach(menuItem -> menuItem.setVisible(true));

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    if (MENU_ITEM_IDS.contains(itemId)) {
      switch (itemId) {
        case R.id.action_help:
          Intent browserIntent = new Intent(Intent.ACTION_VIEW, HELP_URI);
          startActivity(browserIntent);
          break;

        case R.id.action_refresh:
          handleRefresh();
          break;

        case R.id.action_direction:
          startActivity(new Intent(this, CompassActivity.class));
          break;

        case R.id.action_text:
          startActivity(new Intent(this, LiberActivity.class));
          break;

        default:
          throw new AssertionError("Impossible switch default");
      }

      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Subclass implementations of this method are called to determine what options menu items should
   * be visible. The 'Help' menu item is always visible, so it need not be included in this set.
   */
  @ForOverride
  protected abstract Set<Integer> getOptionsMenuItems();

  /**
   * Subclasses which enable the 'Refresh' menu item must override this method with the action to
   * be taken when that item is selected.
   */
  @ForOverride
  protected void handleRefresh() {
    // Do nothing.
  }
}
