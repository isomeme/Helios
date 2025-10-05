package org.onereed.helios;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.onereed.helios.databinding.ActivityMainBinding;
import timber.log.Timber;

/** Main activity for Helios. */
public class MainActivity extends BaseSunInfoActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    setSupportActionBar(activityMainBinding.toolbar);

    SunInfoAdapter sunInfoAdapter = new SunInfoAdapter();

    activityMainBinding.sunEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    activityMainBinding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    sunInfoViewModel.getSunInfoLiveData().observe(this, sunInfoAdapter);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_text, R.id.action_direction);
  }
}
