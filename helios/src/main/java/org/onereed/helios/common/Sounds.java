package org.onereed.helios.common;

import android.media.AudioManager;
import android.media.ToneGenerator;

import org.onereed.helios.BuildConfig;

/**
 * Utility methods for producing indicator sounds. The sounds are intended to support
 * development, and are only enabled in debug mode. To suppress them in debug mode,
 * set {@link #VOLUME} to zero.
 */
public final class Sounds {

  /** Percentage value in the range [0..100]. */
  private static final int VOLUME = 0;

  private static final ToneGenerator TONE_GENERATOR =
      new ToneGenerator(AudioManager.STREAM_ALARM, VOLUME);

  public static void beep() {
    play(ToneGenerator.TONE_PROP_BEEP);
  }

  public static void play(int toneType) {
    if (BuildConfig.DEBUG) {
      TONE_GENERATOR.startTone(toneType);
    }
  }

  private Sounds() {}
}
