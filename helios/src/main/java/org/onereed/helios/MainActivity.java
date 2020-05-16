package org.onereed.helios;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.onereed.helios.common.LocationServiceVerifier;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.common.PlayServicesVerifier;
import org.onereed.helios.databinding.ActivityMainBinding;
import org.onereed.helios.logger.AppLogger;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = LogUtil.makeTag(MainActivity.class);

  private final PlayServicesVerifier playServicesVerifier = new PlayServicesVerifier(this);
  private final LocationServiceVerifier locationServiceVerifier = new LocationServiceVerifier(this);

  private final SunInfoAdapter sunInfoAdapter = new SunInfoAdapter(this);

  private ActivityMainBinding activityMainBinding;
  private LocationManager locationManager;

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

    ViewModelProvider.Factory factory = new ViewModelProvider.NewInstanceFactory();
    SunInfoViewModel sunInfoViewModel =
        new ViewModelProvider(this, factory).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoAdapter::acceptSunInfo);
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
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_refresh) {
      locationManager.requestLastLocation();
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
}
