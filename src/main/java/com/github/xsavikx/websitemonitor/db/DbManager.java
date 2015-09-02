package com.github.xsavikx.websitemonitor.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

public final class DbManager {
  private static final Logger LOGGER = Logger.getLogger(DbManager.class);

  public static void rollbackAndClose(Connection con) {
    LOGGER.debug("rollbackAndClose(Connection) - start");

    try {
      if (con != null && !con.isClosed()) {
        con.rollback();
        con.close();
      }
    } catch (SQLException ex) {
      LOGGER.warn("rollbackAndClose(Connection) - exception ignored", ex);
    }

    LOGGER.debug("rollbackAndClose(Connection) - end");
  }

  public static void commitAndClose(Connection con) {
    LOGGER.debug("commitAndClose(Connection) - start");

    try {
      if (con != null && !con.isClosed()) {
        con.commit();
        con.close();
      }
    } catch (SQLException ex) {
      LOGGER.warn("commitAndClose(Connection) - exception ignored", ex);
    }

    LOGGER.debug("commitAndClose(Connection) - end");
  }

  public static Connection getConnection(DataSource dataSource) {
    LOGGER.debug("getConnection(DataSource) - start");

    if (dataSource == null) {
      throw new IllegalStateException("No dataSource was found");
    }
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    } catch (SQLException e) {
      LOGGER.error("getConnection(DataSource)", e);
      throw new IllegalStateException("Cannot obtain connection from DataSource");
    }

    LOGGER.debug("getConnection(DataSource) - end");
    return conn;
  }
}
