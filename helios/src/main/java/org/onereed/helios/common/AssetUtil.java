package org.onereed.helios.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class AssetUtil {

  public static String readAssetText(Context context, String assetName) {
    try (InputStream inputStream = context.getAssets().open(assetName)) {
      //noinspection NewApi - Handled by desugaring
      return new String(inputStream.readAllBytes(), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private AssetUtil() {}
}
