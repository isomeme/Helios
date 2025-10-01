package org.onereed.helios;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.concurrent.Executor;
import org.onereed.helios.common.LocationUtil;
import org.onereed.helios.databinding.ActivityMainBinding;
import timber.log.Timber;

/** Main activity for Helios. */
public class MainActivity extends AbstractMenuActivity
    implements SwipeRefreshLayout.OnRefreshListener {

  private ActivityMainBinding activityMainBinding;

  private SunInfoViewModel sunInfoViewModel;

  private FusedLocationProviderClient fusedLocationProviderClient;
  private Executor mainExecutor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Timber.d("onCreate");

    activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    setSupportActionBar(activityMainBinding.toolbar);

    LinearLayoutManager sunEventsLayoutManager = new LinearLayoutManager(this);
    activityMainBinding.sunEventsRecyclerView.setLayoutManager(sunEventsLayoutManager);

    SunInfoAdapter sunInfoAdapter = new SunInfoAdapter();
    activityMainBinding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    activityMainBinding.swipeRefresh.setOnRefreshListener(this);

    sunInfoViewModel = new ViewModelProvider(this).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoAdapter::acceptSunInfo);
    sunInfoViewModel
        .getLastUpdateTimeLiveData()
        .observe(this, unusedInstant -> activityMainBinding.swipeRefresh.setRefreshing(false));

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    mainExecutor = ContextCompat.getMainExecutor(this);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_text, R.id.action_direction);
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onResume() {
    Timber.d("onResume");
    super.onResume();

    fusedLocationProviderClient
        .requestLocationUpdates(
            LocationUtil.REPEATED_LOCATION_REQUEST, mainExecutor, sunInfoViewModel)
        .addOnSuccessListener(unusedVoid -> Timber.d("Location updates started."))
        .addOnFailureListener(e -> Timber.e(e, "Location updates start failed."));
  }

  @Override
  public void onPause() {
    Timber.d("onPause");
    super.onPause();

    fusedLocationProviderClient
        .removeLocationUpdates(sunInfoViewModel)
        .addOnSuccessListener(unusedVoid -> Timber.d("Location updates stopped."))
        .addOnFailureListener(e -> Timber.e(e, "Location updates stop failed."));
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onRefresh() {
    fusedLocationProviderClient
        .getLastLocation()
        .addOnSuccessListener(sunInfoViewModel::onLocationChanged)
        .addOnSuccessListener(location -> Timber.d("Swipe-refresh Location update: %s", location))
        .addOnFailureListener(e -> Timber.e(e, "Swipe-refresh location update failed."));
  }
}
