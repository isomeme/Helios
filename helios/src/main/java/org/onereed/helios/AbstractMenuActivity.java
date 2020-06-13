package org.onereed.helios;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.ForOverride;

import java.util.Map;

/** Parent class for activities sharing the common Helios menu. */
abstract class AbstractMenuActivity extends AppCompatActivity {

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);

    getMenuActions().keySet().stream()
        .map(menu::findItem)
        .forEach(menuItem -> menuItem.setVisible(true));

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Runnable action = getMenuActions().get(item.getItemId());

    if (action != null) {
      action.run();
      return true;
    }

    if (item.getItemId() == R.id.action_help) {
      Intent browserIntent =
          new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.one-reed.org/helios"));
      startActivity(browserIntent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Returns a {@link Map} from menu item resource IDs to the action which should be taken when that
   * menu item is selected. Only menu items with IDs that appear as keys in the map are made
   * visible. This default implementation returns an empty map.
   *
   * <p>In addition to whatever is specified in this call, all menus have the 'Help' menu item,
   * which links to the Helios webpage.
   */
  @ForOverride
  protected Map<Integer, Runnable> getMenuActions() {
    return ImmutableMap.of();
  }
}
