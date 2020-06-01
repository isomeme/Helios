package org.onereed.helios;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.ImmutableMap;

import org.onereed.helios.common.LocationServiceVerifier;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.PlayServicesVerifier;
import org.onereed.helios.databinding.ActivityMainBinding;
import org.onereed.helios.logger.AppLogger;

import java.time.Instant;
import java.util.Map;

/** Main activity for Helios. */
public class MainActivity extends AbstractMenuActivity
    implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = LogUtil.makeTag(MainActivity.class);

  private final PlayServicesVerifier playServicesVerifier = new PlayServicesVerifier(this);
  private final LocationServiceVerifier locationServiceVerifier = new LocationServiceVerifier(this);
  private final SunInfoAdapter sunInfoAdapter = new SunInfoAdapter(this);

  private ActivityMainBinding activityMainBinding;
  private LocationManager locationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if ((this.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
      AppLogger.useAndroidLogger();
    }

    AppLogger.debug(TAG, "onCreate");

    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    setSupportActionBar(activityMainBinding.toolbar);

    RecyclerView.LayoutManager sunEventsLayoutManager = new LinearLayoutManager(this);
    activityMainBinding.sunEventsRecyclerView.setLayoutManager(sunEventsLayoutManager);
    activityMainBinding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    activityMainBinding.swipeRefresh.setOnRefreshListener(this);

    ViewModelProvider.Factory factory = new SunInfoViewModelFactory();
    SunInfoViewModel sunInfoViewModel =
        new ViewModelProvider(this, factory).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoAdapter::acceptSunInfo);
    sunInfoViewModel.getLastUpdateTimeLiveData().observe(this, this::updateCompleted);
    locationManager = new LocationManager(this, sunInfoViewModel::acceptLocation);

    getLifecycle().addObserver(playServicesVerifier);
    getLifecycle().addObserver(locationServiceVerifier);
    getLifecycle().addObserver(locationManager);
  }

  @Override
  public void onResume() {
    super.onResume();
    AppLogger.debug(TAG, "onResume");
  }

  @Override
  public void onPause() {
    super.onPause();
    AppLogger.debug(TAG, "onPause");
  }

  @Override
  protected Map<Integer, Runnable> getMenuActions() {
    return ImmutableMap.of(
        R.id.action_refresh,
        () -> {
          activityMainBinding.swipeRefresh.setRefreshing(true);
          requestLocationUpdate();
        },
        R.id.action_direction,
        () -> startActivity(new Intent(this, CompassActivity.class)));
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

  /** This is called from swipe-up gesture refresh. Menu-driven refresh is handled separately. */
  @Override
  public void onRefresh() {
    requestLocationUpdate();
  }

  private void requestLocationUpdate() {
    locationManager.requestLastLocation();
  }

  private void updateCompleted(Instant ignored) {
    activityMainBinding.swipeRefresh.setRefreshing(false);
  }
}
