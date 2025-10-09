package org.onereed.helios;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    hapticClick();

    int id = item.getItemId();

    if (id == R.id.action_schedule) {
      startActivity(new Intent(this, MainActivity.class));
    } else if (id == R.id.action_text) {
      startActivity(new Intent(this, LiberActivity.class));
    } else if (id == R.id.action_compass) {
      startActivity(new Intent(this, CompassActivity.class));
    } else if (id == R.id.action_help) {
      openHelp();
    } else {
      return super.onOptionsItemSelected(item);
    }

    return true;
  }

  private void openHelp() {
    try {
      startActivity(HELP_INTENT);
    } catch (ActivityNotFoundException e) {
      hapticReject();
      runOnUiThread(
          () ->
              Toast.makeText(this, getString(R.string.toast_no_browser), Toast.LENGTH_LONG).show());
    }
  }

  private void hapticClick() {
    hapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
  }

  private void hapticReject() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      hapticFeedback(HapticFeedbackConstants.REJECT);
    }
  }

  private void hapticFeedback(int feedbackConstant) {
    getWindow().getDecorView().performHapticFeedback(feedbackConstant);
  }
}
