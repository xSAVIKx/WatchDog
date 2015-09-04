/**
 * Master thread,
 * checks the various sources for actions and initiate them.
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.constants.ApplicationConstants;
import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;
import com.github.xsavikx.websitemonitor.db.dao.DAOFactory;
import com.github.xsavikx.websitemonitor.db.dao.WatchDogDAO;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;
import com.github.xsavikx.websitemonitor.mailer.Mailer;
import com.github.xsavikx.websitemonitor.timertasks.helper.FeatureHelper;

public class MasterTask extends TimerTask {

  public MasterTask(MonitorTask monitor) {
    this.monitor = monitor;
  }

  // Logger
  private static Logger LOGGER = Logger.getLogger(MasterTask.class);

  // reference to monitor task
  MonitorTask monitor;

  // timestamp of last run of master thread
  private static String lastRun = FeatureHelper.getCurrentDateTime();

  @Override
  public void run() {
    LOGGER.debug(FeatureHelper.getCurrentDateTime() + ":start to check........");

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
        if (tasksToCheck.size() > 0) {
          LOGGER.info("Checking " + routine + " database routine");
          for (WatchDogCheck check : tasksToCheck) {
            Timer timeIt = new Timer(check.getCheckType().getDbAlias() + "#" + check.getUrlToCheck() + "#" + "Timer");
            WatchDogCheckTask checkTask = new WatchDogCheckTask(check, timeIt);
            monitor.addTaskToList(checkTask);
            timeIt.schedule(checkTask, delay); // tested repeatedly, should
                                               // never
                                               // happen
            // as cancel() is in place
            delay += ApplicationConstants.delayBetweenRecord;
          }
          LOGGER.info("Created " + tasksToCheck.size() + " check tasks for " + routine + " database routine");
        } else {
          LOGGER.info("Nothing to check for " + routine + " database routine");
        }
      }

      /**
       * update lastrun marker, but only if this run of the thread is
       * successfull
       */
      lastRun = FeatureHelper.getCurrentDateTime();

      /**
       * If error during a master run then the lastrun marker will not get
       * updated this will result in the timestamp on the check page to get old
       * ( http://www. ( installwebsite ) .com/SBS_PageWatch/PageWatchMaster.jsp
       * ) that will get picked up by another PageWatch on another server who in
       * turn will generate errors to the administrator
       */
    } catch (Exception e) {
      LOGGER.error("Error during processing of the Master Thread", e);
      // attempt to notify administrator
      try {
        Mailer.sendMail("Admin", "Error during master task", "Error during run of master task for SBS_PageWatch.");
      } catch (Exception ee) {
        LOGGER.warn("run() - exception ignored", ee);

        // no a lot to do if this goes wrong
      }
    }

    LOGGER.debug("run() - end");
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
