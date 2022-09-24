package org.onereed.helios;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.ImmutableSet;

import org.onereed.helios.common.LocationServiceVerifier;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.PlayServicesVerifier;
import org.onereed.helios.databinding.ActivityMainBinding;
import org.onereed.helios.location.LocationManager;
import org.onereed.helios.logger.AppLogger;

import java.time.Instant;
import java.util.Set;

/** Main activity for Helios. */
public class MainActivity extends AbstractMenuActivity
    implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = LogUtil.makeTag(MainActivity.class);

  private final PlayServicesVerifier playServicesVerifier = new PlayServicesVerifier(this);
  private final LocationServiceVerifier locationServiceVerifier = new LocationServiceVerifier(this);
  private final SunInfoAdapter sunInfoAdapter = new SunInfoAdapter();

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

    var sunEventsLayoutManager = new LinearLayoutManager(this);
    activityMainBinding.sunEventsRecyclerView.setLayoutManager(sunEventsLayoutManager);
    activityMainBinding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    activityMainBinding.swipeRefresh.setOnRefreshListener(this);

    var sunInfoViewModel =
        new ViewModelProvider(this).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoAdapter::acceptSunInfo);
    sunInfoViewModel.getLastUpdateTimeLiveData().observe(this, this::updateCompleted);
    locationManager = new LocationManager(this, sunInfoViewModel::acceptPlace);

    getLifecycle().addObserver(playServicesVerifier);
    getLifecycle().addObserver(locationServiceVerifier);
    getLifecycle().addObserver(locationManager);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_text, R.id.action_direction);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    locationServiceVerifier.acceptActivityResult(requestCode, resultCode);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onRefresh() {
    locationManager.requestLastLocation();
  }

  private void updateCompleted(Instant ignored) {
    activityMainBinding.swipeRefresh.setRefreshing(false);
  }
}
