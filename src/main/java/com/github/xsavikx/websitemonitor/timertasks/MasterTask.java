/**
 * Master thread,
 * checks the various sources for actions and initiate them.
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.constants.ApplicationConstants;
import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;
import com.github.xsavikx.websitemonitor.db.dao.DAOFactory;
import com.github.xsavikx.websitemonitor.db.dao.WatchDogDAO;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;
import com.github.xsavikx.websitemonitor.helper.Tooler;
import com.github.xsavikx.websitemonitor.mailer.Mailer;

public class MasterTask extends TimerTask {

  public MasterTask(MonitorTask monitor) {
    this.monitor = monitor;
  }

  // Logger
  private static Logger LOGGER = Logger.getLogger(MasterTask.class);

  // reference to monitor task
  MonitorTask monitor;

  // timestamp of last run of master thread
  private static String lastRun = Tooler.getGMT(new Date());

  @Override
  public void run() {
    LOGGER.debug(Tooler.getGMT(new Date()) + ":start to check........");

    /**
     * Read database for pages to be checked in the next 10 minutes and for each
     * kick-off a check thread.
     */
    try {
      /**
       * select * from pagewatch where ( lastchecked + frequency ) < now()
       * 
       * If a page is set to be checked once per hour and was last checked at
       * 12:00 running at 12:40 will not return the page as 12:00 + 60 = 13:00
       * is not yet in the past running at 13:10 will return the page as 12:00 +
       * 60 = 13:00 is in the past so now that page needs to be checked again
       * 
       */
      /**
       * Create a monitor object to monitor other task, to be reviewed later,
       * could be used to monitor the duration of a task
       */
      int delay = 0;
      for (DatabaseRoutine routine : DatabaseRoutine.getEnabledDatabaseRoutines()) {
        WatchDogDAO dao = DAOFactory.getInstance().getDAOBySource(routine);
        List<WatchDogCheck> tasksToCheck = dao.getTasksToCheck();
        for (WatchDogCheck check : tasksToCheck) {
          Timer timeIt = new Timer();
          WatchDogCheckTask pct = new WatchDogCheckTask(check, timeIt);
          monitor.addTaskToList(pct);
          timeIt.schedule(pct, delay); // tested repeatedly, should never happen
                                       // as cancel() is in place
          delay += ApplicationConstants.delayBetweenRecord;
        }
      }

      /**
       * update lastrun marker, but only if this run of the thread is
       * successfull
       */
      lastRun = Tooler.getGMT(new Date());

      /**
       * If error during a master run then the lastrun marker will not get
       * updated this will result in the timestamp on the check page to get old
       * ( http://www. ( installwebsite ) .com/SBS_PageWatch/PageWatchMaster.jsp
       * ) that will get picked up by another PageWatch on another server who in
       * turn will generate errors to the administrator
       */
    } catch (Exception e) {
      LOGGER.error("Error during processing of the Master Thread", e);
      if (LOGGER.isDebugEnabled()) {
        e.printStackTrace();
      }
      // attempt to notify administrator
      try {
        Mailer.sendMail("Admin", "Error during master task", "Error during run of master task for SBS_PageWatch.");
      } catch (Exception ee) {
        // no a lot to do if this goes wrong
        if (LOGGER.isDebugEnabled()) {
          ee.printStackTrace();
        }
      }
    }

  } // end run

  /**
   * Method to return the lastrun marker. This can be used by a webpage to
   * display the last time the master has run.
   * 
   * @return String lastrun
   */
  public static String getLastrun() {
    return lastRun;
  }
}
