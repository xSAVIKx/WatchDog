package com.github.xsavikx.websitemonitor.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public final class DataSourceManager {
  private static final Logger LOGGER = Logger.getLogger(DataSourceManager.class);

  private static DataSource dataSource;
  private static final String LOOKUP_LOCATION = "java:comp/env/jdbc/website_monitor";

  public static DataSource getDataSource() {
    LOGGER.debug("getDataSource() - start");

    if (dataSource == null) {

      try {
        Context initialContext = new InitialContext();
        dataSource = (DataSource) initialContext.lookup(LOOKUP_LOCATION);
      } catch (NamingException e) {
        LOGGER.error("getDataSource()", e);
      }
    }

    LOGGER.debug("getDataSource() - end");
    return dataSource;
  }
}
