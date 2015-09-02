package com.github.xsavikx.websitemonitor.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.db.DbManager;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;

public class WatchDogTimestampDAO extends AbstractWatchDogDAO {

  private static Logger logger = Logger.getLogger(WatchDogWebsiteDAO.class);
  private static final String SQL__SELECT_WATCHDOG_WEBSITE_IN_INTERVAL = "SELECT website_id AS reference_id, website_url AS URL_to_check, maximum_age FROM watchdog_timestamp AS watch WHERE ( lastverified + INTERVAL frequency MINUTE) < UTC_TIMESTAMP() OR lastverified is null ORDER BY lastverified asc LIMIT 100";

  @Override
  public List<WatchDogCheck> getTasksToCheck() {
    // set datasource as class variable if not already set
    if (!checkDataSource()) {
      return null;
    }
    // database connection object
    Connection conn = null;

    try {
      conn = DbManager.getConnection(dataSource);

      /**
       * Retrieve pages that are to be checked.
       * 
       * Master thread runs every 10 minutes so assuming a typical 5 seconds per
       * check it can handle 120 in 10 minutes. So we limit the number to 100
       * with the shortest frequencies first ( assuming the high frequencies are
       * of lesser importance and will simply be checked on one of the next runs
       * )
       * 
       * Note: If the masterthread runs again and one of the tasks has not yet
       * completed then a task for that page will be scheduled twice. The
       * monitor thread should detect tasks that take really long report a
       * warning and remove them. Otherwise the number of threads will grow
       * without stopping.
       * 
       * Alternatively a checktask could mark it's running in the DB, to be
       * examined if there should be a need.
       */

      /*
       * SELECT website_id AS reference_id, website_url AS URL_to_check,
       * maximum_age FROM watchdog_timestamp AS watch WHERE ( lastverified +
       * INTERVAL frequency MINUTE) < UTC_TIMESTAMP() OR lastverified is null
       * ORDER BY lastverified asc LIMIT 100 ;
       */
      try (PreparedStatement statement = conn.prepareStatement(SQL__SELECT_WATCHDOG_WEBSITE_IN_INTERVAL)) {
        try (ResultSet todoList = statement.executeQuery()) {
          // list to return, could be null or empty !
          List<WatchDogCheck> checkList = new ArrayList<WatchDogCheck>();
          // TODO check size of list, if actually 100 then email a notification
          // to
          // the administrator
          while (todoList.next()) {
            WatchDogCheck WatchDogCheck = new WatchDogCheck();
            WatchDogCheck.setSource("watchdog_timestamp");
            WatchDogCheck.setReferenceId(todoList.getInt("reference_id"));
            WatchDogCheck.setCheckType("timestamp");
            WatchDogCheck.setUrlToCheck(todoList.getString("url_to_check"));
            WatchDogCheck.setMaximumAge(todoList.getInt("maximum_age"));
            checkList.add(WatchDogCheck);
          }
          if (checkList.size() >= MAXIMUM_CHECK_LIST_SIZE) {
            // send email to admin here
          }
          return checkList;
        }
      }
    } catch (SQLException e) {
      DbManager.rollbackAndClose(conn);
      logger.error("Error in SBS_WatchDog.getPagestocheck", e);
      e.printStackTrace();
    } finally {
      DbManager.commitAndClose(conn);
    }
    return null;
  }

  private static final String SQL__UPDATE_WATCHDOG_TIMESTAMP_SET_LASTVERIFIED_BY_WEBSITE_ID = "UPDATE watchdog_timestamp SET lastverified = UTC_TIMESTAMP() WHERE website_id = ?";
  private static final String SQL__UPDATE_WATCHDOG_TIMESTAMP_LOG_SET_LASTENCOUNTERED_BY_WEBSITE_ID = "UPDATE watchdog_timestamp_log SET last_encountered = UTC_TIMESTAMP() WHERE website_id = ?";
  private static final String SQL__SELECT_WATCHDOG_TIMESTAMP_LOG_BY_WEBSITE_ID = "SELECT id, return_status_code, return_status_text, timestamp_found FROM watchdog_timestamp_log WHERE website_id = ? ORDER BY last_encountered DESC";
  private static final String SQL__INSERT_INTO_WATCHDOG_TIMESTAMP_LOG = "INSERT INTO watchdog_timestamp_log(website_id, first_encountered,last_encountered,return_status_code,return_status_text, timestamp_found VALUES(? ,UTC_TIMESTAMP(),UTC_TIMESTAMP(), ?, ?, ?)";

  /***************************************************************************
   * write the check result to the database
   */
  @Override
  public void storeResult(WatchDogCheck checkresult) {
    // set datasource as class variable if not already set
    if (!checkDataSource()) {
      return;
    }

    // TODO check if the object contains as source the name "watchdog_timestamp"

    // database connection object
    Connection DBconnection = null;
    try {
      DBconnection = DbManager.getConnection(dataSource);

      /**
       * Store the last error result with the page entry
       */
      /*
       * UPDATE watchdog_timestamp SET lastverified = UTC_TIMESTAMP() WHERE
       * website_id = ? ;
       */
      try (PreparedStatement statement = DBconnection
          .prepareStatement(SQL__UPDATE_WATCHDOG_TIMESTAMP_SET_LASTVERIFIED_BY_WEBSITE_ID)) {
        statement.setInt(1, checkresult.getReferenceId());
        statement.executeUpdate();
      }

      /**
       * retrieve last entry from log, insert new record or update existing one
       */
      /*
       * SELECT id, return_status_code, return_status_text, timestamp_found FROM
       * watchdog_timestamp_log WHERE website_id = ? ORDER BY last_encountered
       * DESC LIMIT 1 ;
       */
      try (PreparedStatement watchdogTimestampLogPS = DBconnection
          .prepareStatement(SQL__SELECT_WATCHDOG_TIMESTAMP_LOG_BY_WEBSITE_ID);) {
        watchdogTimestampLogPS.setInt(1, checkresult.getReferenceId());
        try (ResultSet rs = watchdogTimestampLogPS.executeQuery();) {
          if (rs.next()) {

            // existing record
            // retrieve last status
            int lastStatusID = rs.getInt("id");
            String lastStatusCode = rs.getString("return_status_code");
            String lastStatusText = rs.getString("return_status_text");
            boolean lastStatus_timestamp = rs.getBoolean("timestamp_found");

            /**
             * if resultcheck = last
             */
            if (checkresult.getResponseCode().equalsIgnoreCase(lastStatusCode)) {
              /*
               * UPDATE watchdog_website_log SET last_encountered =
               * UTC_TIMESTAMP() WHERE id = ?;
               */
              try (PreparedStatement statement = DBconnection
                  .prepareStatement(SQL__UPDATE_WATCHDOG_TIMESTAMP_LOG_SET_LASTENCOUNTERED_BY_WEBSITE_ID);) {
                statement.setInt(1, lastStatusID);
                statement.executeUpdate();
              }
            } else {
              // from ERROR to OK , enter new record
              // from OK to ERROR, enter new record
              /*
               * INSERT INTO watchdog_website_log
               * (id,website_id,first_encountered,
               * last_encountered,return_status_code,return_status_text)
               * VALUES(null, ? ,UTC_TIMESTAMP(),UTC_TIMESTAMP(), ?, ?) ;
               */
              try (PreparedStatement statement = DBconnection.prepareStatement(SQL__INSERT_INTO_WATCHDOG_TIMESTAMP_LOG);) {
                statement.setInt(1, checkresult.getReferenceId());
                statement.setString(2, checkresult.getResponseCode());
                statement.setString(3, checkresult.getResponseText());
                statement.setBoolean(4, checkresult.isTimestampFound());
                statement.executeUpdate();
                statement.close();

                // if return code changes then notify administrator
                if (1 == 1) {
                  // email notification
                  // status changed from to ;
                }
              }
            }
          } else {
            // no existing record
            // enter new record
            /*
             * INSERT INTO watchdog_website_log
             * (id,website_id,first_encountered,last_encountered
             * ,return_status_code,return_status_text) VALUES(null, ?
             * ,UTC_TIMESTAMP(),UTC_TIMESTAMP(), ?, ?) ;
             */
            try (PreparedStatement statement = DBconnection.prepareStatement(SQL__INSERT_INTO_WATCHDOG_TIMESTAMP_LOG);) {
              statement.setInt(1, checkresult.getReferenceId());
              statement.setString(2, checkresult.getResponseCode());
              statement.setString(3, checkresult.getResponseText());
              statement.executeUpdate();

              // notify administrator only if code is unreachable with first
              // check.
              if (1 == 1) {
                // ok then no action
              } else {
                // email notification
                // first checked, not reachable;
              }
            }
          }
        }
      }

    } catch (Exception e) {
      DbManager.rollbackAndClose(DBconnection);
      logger.error("Error in SBS_WatchDog.DAO_watchdog_website.storeResult", e);
    } finally {
      DbManager.commitAndClose(DBconnection);
    }
    return;
  }
}
