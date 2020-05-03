package org.onereed.helios;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.onereed.helios.common.LocationServiceVerifier;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.PlayServicesVerifier;
import org.onereed.helios.common.ToastUtil;
import org.onereed.helios.databinding.ActivityMainBinding;
import org.onereed.helios.sun.SunCalculator;
import org.onereed.helios.sun.SunInfo;

import java.lang.ref.WeakReference;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = LogUtil.makeTag(MainActivity.class);

  private final PlayServicesVerifier playServicesVerifier = new PlayServicesVerifier(this);
  private final LocationServiceVerifier locationServiceVerifier = new LocationServiceVerifier(this);
  private final LocationManager locationManager = new LocationManager(this);

  private final MainHandler mainHandler = new MainHandler(this);
  private final SunCalculator sunCalculator = new SunCalculator(mainHandler::acceptSunInfo);

  private ActivityMainBinding activityMainBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);

    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

    setContentView(activityMainBinding.getRoot());
    setSupportActionBar(activityMainBinding.toolbar);

    getLifecycle().addObserver(playServicesVerifier);
    getLifecycle().addObserver(locationServiceVerifier);
    getLifecycle().addObserver(locationManager);
  }

  @Override
  public void onResume() {
    Log.d(TAG, "onResume");
    super.onResume();
    updateSun();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause");
    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_refresh) {
      updateSun();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    Log.d(TAG, "onActivityResult");
    super.onActivityResult(requestCode, resultCode, intent);
    locationServiceVerifier.acceptActivityResult(requestCode, resultCode);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "onRequestPermissionsResult");
    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
  }

  private void updateSun() {
    locationManager.requestLocation(
        location -> {
          if (location == null) {
            Log.e(TAG, "Location is null.");
            ToastUtil.longToast(this, R.string.toast_location_failure);
          } else {
            sunCalculator.acceptLocation(location);
          }
        });
  }

  private void display(SunInfo sunInfo) {
    Date date = new Date();
    String text = String.format("%s\n\n%s", date, sunInfo);
    activityMainBinding.textSun.setText(text);
  }

  private static class MainHandler extends Handler {

    private final WeakReference<MainActivity> mainActivityRef;

    private MainHandler(MainActivity mainActivity) {
      this.mainActivityRef = new WeakReference<>(mainActivity);
    }

    private void acceptSunInfo(SunInfo sunInfo) {
      this.sendMessage(this.obtainMessage(0, sunInfo));
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      MainActivity mainActivity = mainActivityRef.get();

      if (mainActivity != null) {
        if (msg.what == 0) {
          SunInfo sunInfo = (SunInfo) msg.obj;
          mainActivity.display(sunInfo);
        }
      }
    }
  }
}
