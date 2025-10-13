package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.errorprone.annotations.ForOverride;

/** Parent class for Helios activities. */
public abstract class BaseActivity extends AppCompatActivity {

  private static final String HELP_PAGE = "https://www.one-reed.org/helios";
  private static final Intent HELP_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse(HELP_PAGE));

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setEnterTransition(new Fade());
    getWindow().setExitTransition(new Fade());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actions_menu, menu);

    ColorStateList colors =
        ContextCompat.getColorStateList(this, R.color.actions_menu_color_selector);

    for (int i = 0; i < menu.size(); i++) {
      menu.getItem(i).setIconTintList(colors);
    }

    checkNotNull(menu.findItem(myActionsMenuId())).setEnabled(false);

    return super.onCreateOptionsMenu(menu);
  }

  /**
   * Subclass implementations of this method provide their actions menu ID so we can disable that
   * control in the nav bar.
   */
  @ForOverride
  protected abstract int myActionsMenuId();

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    hapticClick();

    int id = item.getItemId();

    if (id == R.id.action_schedule) {
      go(new Intent(this, MainActivity.class));
    } else if (id == R.id.action_text) {
      go(new Intent(this, LiberActivity.class));
    } else if (id == R.id.action_compass) {
      go(new Intent(this, CompassActivity.class));
    } else if (id == R.id.action_help) {
      openHelp();
    } else {
      return super.onOptionsItemSelected(item);
    }

    return true;
  }

  private void openHelp() {
    try {
      go(HELP_INTENT);
    } catch (ActivityNotFoundException e) {
      hapticReject();
      runOnUiThread(
          () ->
              Toast.makeText(this, getString(R.string.toast_no_browser), Toast.LENGTH_LONG).show());
    }
  }

  private void go(Intent intent) {
    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
    startActivity(intent, bundle);
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
