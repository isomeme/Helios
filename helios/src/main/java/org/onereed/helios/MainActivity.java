package org.onereed.helios;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.onereed.helios.databinding.ActivityMainBinding;
import timber.log.Timber;

/** Main activity for Helios. */
public class MainActivity extends BaseSunInfoActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbar);

    SunInfoAdapter sunInfoAdapter = new SunInfoAdapter(this);

    binding.sunEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    binding.sunEventsRecyclerView.setAdapter(sunInfoAdapter);

    observeSunInfo(sunInfoAdapter);
  }

  @Override
  protected int myActionsMenuId() {
    return R.id.action_schedule;
  }
}
