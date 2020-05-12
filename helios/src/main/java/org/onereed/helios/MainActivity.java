package org.onereed.helios;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;

import org.onereed.helios.common.LocationServiceVerifier;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.PlayServicesVerifier;
import org.onereed.helios.databinding.ActivityMainBinding;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEngine;
import org.onereed.helios.sun.SunInfo;

import java.lang.ref.WeakReference;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = LogUtil.makeTag(MainActivity.class);

  private static final long LOCATION_STALE_NANOS = Duration.ofMinutes(10L).toNanos();

  private final Clock clock = Clock.systemUTC();
  private final Executor backgroundExecutor = Executors.newWorkStealingPool();

  private final PlayServicesVerifier playServicesVerifier = new PlayServicesVerifier(this);
  private final LocationServiceVerifier locationServiceVerifier = new LocationServiceVerifier(this);
  private final LocationManager locationManager = new LocationManager(this);

  private final MainHandler mainHandler = new MainHandler(this);
  private final SunInfoAdapter sunInfoAdapter = new SunInfoAdapter(this);

  private final SunEngine sunEngine = new SunEngine(clock);

  private ActivityMainBinding activityMainBinding;

  private Location latestLocation = null;
  private SunInfo latestSunInfo = SunInfo.EMPTY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppLogger.debug(TAG, "onCreate");

    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    setSupportActionBar(activityMainBinding.toolbar);

    RecyclerView.LayoutManager sunEventsLayoutManager = new LinearLayoutManager(this);
    activityMainBinding.sunEventsRecyclerView.setLayoutManager(sunEventsLayoutManager);
    activityMainBinding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    getLifecycle().addObserver(playServicesVerifier);
    getLifecycle().addObserver(locationServiceVerifier);
    getLifecycle().addObserver(locationManager);
  }

  @Override
  public void onResume() {
    super.onResume();
    AppLogger.debug(TAG, "onResume");
    mainHandler.requestUpdate(false);
  }

  @Override
  public void onPause() {
    super.onPause();
    AppLogger.debug(TAG, "onPause");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_refresh) {
      mainHandler.requestUpdate(true);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    AppLogger.debug(TAG, "onActivityResult");
    super.onActivityResult(requestCode, resultCode, intent);
    locationServiceVerifier.acceptActivityResult(requestCode, resultCode);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    AppLogger.debug(TAG, "onRequestPermissionsResult");
    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
  }

  private void update(boolean isForced) {
    long lastLocationAgeNanos =
        latestLocation == null
            ? Long.MAX_VALUE
            : SystemClock.elapsedRealtimeNanos() - latestLocation.getElapsedRealtimeNanos();
    boolean isLastLocationStale = lastLocationAgeNanos > LOCATION_STALE_NANOS;
    boolean showProgressBar = false;

    AppLogger.debug(
        TAG, "update, isForced=%s, isLastLocationStale=%s", isForced, isLastLocationStale);

    if (isForced || isLastLocationStale) {
      // If requestLocation returns false, it means that the user is being prompted for location
      // permission, and we're headed into paused state.

      showProgressBar = locationManager.requestLocation(mainHandler::acceptLocation);
    } else if (latestSunInfo.getStaleTime().isBefore(clock.instant())) {
      showProgressBar = true;
      startSunInfoCalculation(latestLocation);
    }

    if (showProgressBar) {
      activityMainBinding.progressBar.setVisibility(View.VISIBLE);
    }
  }

  private void acceptLocation(Location location) {
    latestLocation = location;
    startSunInfoCalculation(location);
  }

  private void startSunInfoCalculation(Location location) {
    Tasks.call(backgroundExecutor, () -> sunEngine.locationToSunInfo(location))
        .addOnSuccessListener(mainHandler::acceptSunInfo)
        .addOnFailureListener(
            t -> {
              activityMainBinding.progressBar.setVisibility(View.INVISIBLE);
              AppLogger.error(TAG, t, "Error from SunEngine.");
            });
  }

  private void acceptSunInfo(SunInfo sunInfo) {
    activityMainBinding.progressBar.setVisibility(View.INVISIBLE);
    latestSunInfo = sunInfo;
    sunInfoAdapter.acceptSunInfo(sunInfo);
  }

  private static class MainHandler extends Handler {

    private static final int UPDATE_MSG = 0;
    private static final int LOCATION_MSG = 1;
    private static final int SUN_INFO_MSG = 2;

    private final WeakReference<MainActivity> mainActivityRef;

    private MainHandler(MainActivity mainActivity) {
      this.mainActivityRef = new WeakReference<>(mainActivity);
    }

    private void requestUpdate(boolean isForced) {
      sendMessage((this.obtainMessage(UPDATE_MSG, isForced)));
    }

    private void acceptLocation(Location location) {
      sendMessage(this.obtainMessage(LOCATION_MSG, location));
    }

    private void acceptSunInfo(SunInfo sunInfo) {
      sendMessage(this.obtainMessage(SUN_INFO_MSG, sunInfo));
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      MainActivity mainActivity = mainActivityRef.get();
      if (mainActivity == null) {
        return;
      }

      switch (msg.what) {
        case UPDATE_MSG:
          boolean isForced = (Boolean) msg.obj;
          mainActivity.update(isForced);
          break;
        case LOCATION_MSG:
          Location location = (Location) msg.obj;
          mainActivity.acceptLocation(location);
          break;
        case SUN_INFO_MSG:
          SunInfo sunInfo = (SunInfo) msg.obj;
          mainActivity.acceptSunInfo(sunInfo);
          break;
      }
    }
  }
}
