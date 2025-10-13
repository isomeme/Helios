package org.onereed.helios.common

import android.annotation.SuppressLint
import android.content.Context
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets

object AssetUtil {

    @SuppressLint("NewApi") // Desugared nio readAllBytes()
    @JvmStatic
    fun readAssetText(context: Context, assetName: String): String {
        try {
            context.assets.open(assetName).use { inputStream ->
                return String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
