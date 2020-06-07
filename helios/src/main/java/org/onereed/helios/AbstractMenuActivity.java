package org.onereed.helios;

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

    return super.onOptionsItemSelected(item);
  }

  /**
   * Returns a {@link Map} from menu item resource IDs to the action which should be taken when that
   * menu item is selected. Only menu items with IDs that appear as keys in the map are made
   * visible. This default implementation returns an empty map, meaning no menu items are shown.
   */
  @ForOverride
  protected Map<Integer, Runnable> getMenuActions() {
    return ImmutableMap.of();
  }
}
