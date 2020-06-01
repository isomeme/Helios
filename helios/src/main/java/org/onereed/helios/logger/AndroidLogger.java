package org.onereed.helios.logger;

import android.util.Log;

/** An implementation of {@link GeneralLogger} that uses Android logging. */
class AndroidLogger implements GeneralLogger {

  static AndroidLogger create() {
    return new AndroidLogger();
  }

  private AndroidLogger() {}

  @Override
  public void verbose(String tag, String message) {
    Log.v(tag, message);
  }

  @Override
  public void debug(String tag, String message) {
    Log.d(tag, message);
  }

  @Override
  public void info(String tag, String message) {
    Log.i(tag, message);
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
  public void error(String tag, String message, Throwable tr) {
    Log.e(tag, message, tr);
  }
}
