package com.github.xsavikx.websitemonitor.timertasks;

public enum WatchDogCheckExceptions {
  CONNECTION_TIMEOUT(1, "Connection timed out"), //
  UNKNOWN_HOST(2, "Unknown host"), //
  CLIENT_PROTOCOL_EXCEPTION(3, "Client protocol exception"), //
  IO_EXCEPTION(50, "Input/Output connection exception"), //
  UNKNOWN_EXCEPTION(666, "Unknown exception");

  private int code;
  private String message;

  private WatchDogCheckExceptions(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

}
