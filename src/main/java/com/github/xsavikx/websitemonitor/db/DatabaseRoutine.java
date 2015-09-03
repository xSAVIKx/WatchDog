package com.github.xsavikx.websitemonitor.db;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public enum DatabaseRoutine {
  WATCH_DOG_WEBSITE("DAO_watchdog_website"), //
  WATCH_DOG_TIMESTAMP("DAO_watchdog_timestamp"), //
  PUNTER_INDEX_WEBSITE("DAO_punterindex_website"), //
  PUNTER_INDEX_LINKBACK("DAO_punterindex_linkback"), //
  PUNTER_INDEX_CONTENT("DAO_punterindex_content");

  public static final String IS_ON_VALUE_PATTERN = "on";

  private String source;
  private boolean isOn = false;

  private DatabaseRoutine(String source) {
    this.source = source;
  }

  public String getSource() {
    return source;
  }

  public boolean isOn() {
    return isOn;
  }

  public void setOn(boolean isOn) {
    this.isOn = isOn;
  }

  public static Set<DatabaseRoutine> getEnabledDatabaseRoutines() {
    Set<DatabaseRoutine> enabledRoutines = EnumSet.allOf(DatabaseRoutine.class);
    Iterator<DatabaseRoutine> iterator = enabledRoutines.iterator();
    while (iterator.hasNext()) {
      if (!iterator.next().isOn) {
        iterator.remove();
      }
    }
    return enabledRoutines;
  }

  public static DatabaseRoutine getBySource(String source) {
    for (DatabaseRoutine routine : values()) {
      if (routine.source.equalsIgnoreCase(source)) {
        return routine;
      }
    }
    throw new IllegalArgumentException("No DatabaseRoutine with source=" + source + " was found");
  }
}
