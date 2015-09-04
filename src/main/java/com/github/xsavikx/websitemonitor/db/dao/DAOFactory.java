package com.github.xsavikx.websitemonitor.db.dao;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;

public class DAOFactory {
  private static final Logger LOGGER = Logger.getLogger(DAOFactory.class);

  private static DAOFactory instance;

  private DAOFactory() {
    //
  }

  public static DAOFactory getInstance() {
    LOGGER.debug("getInstance() - start");

    if (instance == null) {
      LOGGER.debug("Instanciating DAOFactory");
      instance = new DAOFactory();
    }

    LOGGER.debug("getInstance() - end");
    return instance;
  }

  public WatchDogDAO getDAOBySource(DatabaseRoutine routine) {
    LOGGER.debug("getDAOBySource(DatabaseRoutine) - start");
    switch (routine) {
      case WATCH_DOG_WEBSITE:
        LOGGER.debug("Getting WatchDogWebsiteDAO");
        LOGGER.debug("getDAOBySource(DatabaseRoutine) - end");
        return new WatchDogWebsiteDAO();
      case WATCH_DOG_TIMESTAMP:
        LOGGER.debug("Getting WatchDogTimestampDAO");
        LOGGER.debug("getDAOBySource(DatabaseRoutine) - end");
        return new WatchDogTimestampDAO();
      default:
        LOGGER.debug("getDAOBySource(DatabaseRoutine) - end");
        throw new IllegalArgumentException("Such DatabaseRoutine=" + routine + " is not yet supported.");
    }
  }
}
