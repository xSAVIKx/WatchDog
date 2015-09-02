package com.github.xsavikx.websitemonitor.db;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public enum DatabaseRoutine {
  WatchDogWebsite("watchdog_website"), //
  WatchDogTimestamp("watchdog_timestamp"), //
  PunterIndexWebsite("punterindex_website"), //
  PunterIndexLinkBack("punterindex_linkback"), //
  PunterIndexContent("punterindex_content");

  private String source;
  private boolean isOn = false;
  private boolean isOverride = false;

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

  public boolean isOverride() {
    return isOverride;
  }

  public void setOverride(boolean isOverride) {
    this.isOverride = isOverride;
  }
}
