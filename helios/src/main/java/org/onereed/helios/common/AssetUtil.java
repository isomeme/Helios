package org.onereed.helios.common;

import static java.util.stream.Collectors.joining;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

public final class AssetUtil {

  public static String readAssetText(Context context, String assetName) {
    try (InputStream inputStream = context.getAssets().open(assetName)) {
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      return bufferedReader.lines().collect(joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private AssetUtil() {}
}
