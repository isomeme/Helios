package org.onereed.helios;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link ViewModelProvider.Factory} for {@link SunInfoViewModel}.
 *
 * <p>Currently the only advantage of this class over {@link ViewModelProvider.NewInstanceFactory}
 * is that it allows {@link SunInfoViewModel} to have package rather than public visibility. It will
 * become necessary if we ever want to have a non-zero-argument constructor for {@link
 * SunInfoViewModel}.
 */
class SunInfoViewModelFactory implements ViewModelProvider.Factory {

  @NonNull
  @Override
  @SuppressWarnings("unchecked") // Needed for cast to (T).
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    checkArgument(modelClass.isAssignableFrom(SunInfoViewModel.class));
    return (T) new SunInfoViewModel();
  }
}
