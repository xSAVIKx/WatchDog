package com.github.xsavikx.websitemonitor.db.model;

public enum WatchDogCheckType {
  WEBSITE("website"), LINKBACK("linkback"), CONTENT("content"), TIMESTAMP("timestamp");
  private String dbAlias;

  private WatchDogCheckType(String dbAlias) {
    this.dbAlias = dbAlias;
  }

  public String getDbAlias() {
    return dbAlias;
  }

  public static WatchDogCheckType getByDatabaseAlias(String dbAlias) {
    for (WatchDogCheckType routine : values()) {
      if (routine.dbAlias.equalsIgnoreCase(dbAlias)) {
        return routine;
      }
    }
    throw new IllegalArgumentException("No WatchDogCheckType with dbAlias=" + dbAlias + " was found");
  }
}
