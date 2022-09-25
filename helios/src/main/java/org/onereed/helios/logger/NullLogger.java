package org.onereed.helios.logger;

/** A {@link GeneralLogger} which doesn't actually log anything. */
class NullLogger implements GeneralLogger {

  private NullLogger() {}

  static NullLogger create() {
    return new NullLogger();
  }

  @Override
  public void debug(String tag, String message) {
    // Do nothing.
  }

  @Override
  public void warning(String tag, String message) {
    // Do nothing.
  }

  @Override
  public void error(String tag, String message) {
    // Do nothing.
  }

  @Override
  public void error(String tag, String message, Throwable tr) {
    // Do nothing.
  }
}
