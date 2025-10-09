package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.Task;
import java.time.Instant;
import org.onereed.helios.common.Place;
import org.onereed.helios.common.Sounds;
import org.onereed.helios.sun.SunInfo;
import org.onereed.helios.sun.SunInfoSource;
import timber.log.Timber;

/** Stores and updates data needed for {@link SunInfo} display. */
// Must be public to work with the default ViewModel provider factory.
public class SunInfoViewModel extends ViewModel implements LocationListener {

  private final MutableLiveData<SunInfo> sunInfoMutableLiveData = new MutableLiveData<>();

  LiveData<SunInfo> getSunInfoLiveData() {
    return sunInfoMutableLiveData;
  }

  @Override
  public void onLocationChanged(@NonNull Location location) {
    Place where = Place.from(location);
    SunInfoSource.request(where, Instant.now()).addOnCompleteListener(this::publishSunInfo);
  }

  private void publishSunInfo(Task<SunInfo> sunInfoTask) {
    if (sunInfoTask.isSuccessful()) {
      Sounds.beep();
      var sunInfo = checkNotNull(sunInfoTask.getResult());
      sunInfoMutableLiveData.postValue(sunInfo);
    } else {
      Timber.e(sunInfoTask.getException(), "Failure obtaining SunInfo.");
    }
  }
}
