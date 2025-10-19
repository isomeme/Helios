package org.onereed.helios.common

import android.annotation.SuppressLint
import android.content.Context
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8

object AssetUtil {

  @SuppressLint("NewApi") // Desugared nio readAllBytes()
  @JvmStatic
  fun readAssetText(context: Context, assetName: String): String {
    try {
      return context.assets.open(assetName).use { String(it.readAllBytes(), UTF_8) }
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }
  }
}
