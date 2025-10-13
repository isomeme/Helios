package org.onereed.helios.common

import android.media.AudioManager
import android.media.ToneGenerator
import org.onereed.helios.BuildConfig

/**
 * Utility methods for producing indicator sounds. The sounds are intended to support
 * development, and are only enabled in debug mode. To suppress them in debug mode,
 * set [.VOLUME] to zero.
 */
object Sounds {

    /** Percentage value in the range [0..100].  */
    private const val VOLUME = 0

    private val TONE_GENERATOR = ToneGenerator(AudioManager.STREAM_ALARM, VOLUME)

    @JvmStatic
    fun beep() {
        play(ToneGenerator.TONE_PROP_BEEP)
    }

    fun play(toneType: Int) {
        if (BuildConfig.DEBUG && VOLUME > 0) {
            TONE_GENERATOR.startTone(toneType)
        }
    }
}
