package com.github.xsavikx.websitemonitor.db.dao;

import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;

public class DAOFactory {
  private static DAOFactory instance;

  private DAOFactory() {

  }

  public static DAOFactory getInstance() {
    if (instance == null) {
      instance = new DAOFactory();
    }
    return instance;
  }

  public WatchDogDAO getDAOBySource(DatabaseRoutine routine) {
    switch (routine) {
      case WatchDogWebsite:
        return new WatchDogWebsiteDAO();
      case WatchDogTimestamp:
        return new WatchDogTimestampDAO();
      default:
        throw new IllegalArgumentException("Such DatabaseRoutine=" + routine + " is not yet supported.");
    }
  }
}
