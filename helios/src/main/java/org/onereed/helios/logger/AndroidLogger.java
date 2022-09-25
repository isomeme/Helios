package org.onereed.helios.logger;

import android.util.Log;

/** An implementation of {@link GeneralLogger} that uses Android logging. */
class AndroidLogger implements GeneralLogger {

  private AndroidLogger() {}

  static AndroidLogger create() {
    return new AndroidLogger();
  }

  @Override
  public void debug(String tag, String message) {
    Log.d(tag, message);
  }

  @Override
  public void warning(String tag, String message) {
    Log.w(tag, message);
  }

  @Override
  public void error(String tag, String message) {
    Log.e(tag, message);
  }

  @Override
  public void error(String tag, String message, Throwable t) {
    Log.e(tag, message, t);
  }
}
