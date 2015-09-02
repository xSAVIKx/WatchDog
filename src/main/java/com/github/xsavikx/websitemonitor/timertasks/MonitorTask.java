/**
 * Checks if the master thread is running and does not get overloaded or timed-out.
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.helper.Tooler;
import com.github.xsavikx.websitemonitor.mailer.Mailer;

public class MonitorTask extends TimerTask {

  private static Logger LOGGER = Logger.getLogger(MonitorTask.class);

  // class variable, remains in existence between runs
  private List<WatchDogCheckTask> taskList = new ArrayList<WatchDogCheckTask>();

  // private variable to hold the last run time,
  // used by the page MonitorWatchMaster.jsp
  private static String lastrun = Tooler.getGMT(new Date());

  public MonitorTask() {
  }

  @Override
  public void run() {

    try {
      /**
       * Remove tasks from the taskList that are no longer running
       */
      List<WatchDogCheckTask> toremove = new ArrayList<WatchDogCheckTask>();
      for (WatchDogCheckTask task : taskList) {
        if (!task.isRunning()) {
          toremove.add(task);
          System.out.println("task is NOT running" + task.getName());
        } else {
          // System.out.println("task="+task.toString());
        }
      }
      for (WatchDogCheckTask removeit : toremove) {
        taskList.remove(removeit);
      }
      // for each task still running display name and starttime
      if (LOGGER.isDebugEnabled()) {
        for (WatchDogCheckTask runningtask : taskList) {
          System.out.println("Tasks now still running : " + runningtask.scheduledExecutionTime() + "---"
              + runningtask.getName());
        }
        System.out.println("Number of tasks now still running : " + taskList.size());
      }

      /**
       * If at any given time there are more than 125 tasks running then there
       * are probably a number of tasks stuck in an error or there are simply
       * too many being scheduled at the same time.
       * 
       * Report situation to administrator and remove all current tasks making
       * sure there's room for the most important ones and this application does
       * not interfere with Tomcat overall by becoming a runaway application.
       */
      if (taskList.size() > 125) {
        String message = "";
        for (WatchDogCheckTask task : taskList) {
          message = message + "Time:" + task.scheduledExecutionTime() + "....." + task.getName() + "\n";
        }
        Mailer.sendMail("Admin", "WARNING PageWatch exceeded 125 tasks", message);
        for (WatchDogCheckTask task : taskList) {
          message = message + "Time:" + task.scheduledExecutionTime() + "....." + task.getName() + "\n";
          // first cancel Timer
          task.timeIt.cancel();
          // cancel task to run again, or even run once
          task.cancel();
          // set to not running so it gets removed on next monitor run
          task.setRunning(false);
        }
      }

      /**
       * TODO , verify duration of each task, if running longer than say 15
       * minutes then report and kill that task
       * 
       * NOTE it is not actually possible to kill a task, it always runs to
       * completion of the run() method, so if it is stuck it will remain stuck.
       * 
       * A possible alternative would be to interrupt the thread running behind
       * the Timer if this should become a problem then al alternative would be
       * to use threads rather than timertasks
       */
      // for ( PageCheckTask task : taskList ) {
      //
      // retrieve task.scheduledExecutionTime();
      // check if it is older than 15 minutes
      //
      // mailer.Mailer.sendMail("Admin", "WARNING task older than 15 minutes",
      // task.getName());
      // task.timeIt.cancel();
      // task.cancel();
      // task.setRunning(false);
      //
      // }

      /**
       * update lastrun marker, but only if this run of the thread is
       * successfull
       */
      lastrun = Tooler.getGMT(new Date());

      /**
       * If error during a master run then the lastrun marker will not get
       * updated this will result in the timestamp on the check page to get old
       * ( http://www. ( installwebsite )
       * .com/SBS_PageWatch/MonitorWatchMaster.jsp ) that will get picked up by
       * another PageWatch on another server who in turn will generate errors to
       * the administrator
       */
    } catch (Exception e) {
      LOGGER.error("Error in SBS_PageWatch monitor thread", e);
      if (LOGGER.isDebugEnabled()) {
        e.printStackTrace();
      }
    }
  }

  public void addTaskToList(WatchDogCheckTask pct) {
    taskList.add(pct);
  }

  /**
   * Method to return the lastrun marker. This can be used by a webpage to
   * display the last time the master has run.
   * 
   * @return String lastrun
   */
  public static String getLastrun() {
    return lastrun;
  }

}
