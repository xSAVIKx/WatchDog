/**
 * Checks if the master thread is running and does not get overloaded or timed-out.
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.mailer.Mailer;
import com.github.xsavikx.websitemonitor.timertasks.helper.FeatureHelper;

public class MonitorTask extends TimerTask {
  private static Logger LOGGER = Logger.getLogger(MonitorTask.class);
  private static final int MAXIMUM_TASKS_AMOUNT = 125;
  private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 900_000;
  private List<WatchDogCheckTask> taskList = new ArrayList<WatchDogCheckTask>();

  // private variable to hold the last run time,
  // used by the page MonitorWatchMaster.jsp
  private String lastRun = FeatureHelper.getCurrentDateTime();

  public MonitorTask() {
  }

  @Override
  public void run() {
    LOGGER.debug("run() - start");

    try {
      removeFinishedTasks();
      displayActiveTasks();
      preventServerCrash();
      checkStuckTasks();
      /**
       * update lastrun marker, but only if this run of the thread is
       * successfull
       */
      lastRun = FeatureHelper.getCurrentDateTime();

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
    }

    LOGGER.debug("run() - end");
  }

  /**
   * Print still running tasks
   */
  private void displayActiveTasks() {
    for (WatchDogCheckTask runningtask : taskList) {
      LOGGER.info("Tasks now still running : " + runningtask.scheduledExecutionTime() + "---"
          + runningtask.getTaskUrl());
    }
    LOGGER.info("Number of tasks now still running : " + taskList.size());
  }

  /**
   * Remove tasks from the taskList that are no longer running
   */
  private void removeFinishedTasks() {
    Iterator<WatchDogCheckTask> iterator = taskList.iterator();
    while (iterator.hasNext()) {
      WatchDogCheckTask task = iterator.next();
      if (!task.isRunning()) {
        iterator.remove();
        LOGGER.info("Task is NOT running. URL: " + task.getTaskUrl());
      } else {
        LOGGER.info("Task is running. URL: " + task.getTaskUrl());
      }
    }
  }

  /**
   * If at any given time there are more than 125 tasks running then there are
   * probably a number of tasks stuck in an error or there are simply too many
   * being scheduled at the same time.
   * 
   * Report situation to administrator and remove all current tasks making sure
   * there's room for the most important ones and this application does not
   * interfere with Tomcat overall by becoming a runaway application.
   */
  private void preventServerCrash() {
    if (taskList.size() > MAXIMUM_TASKS_AMOUNT) {
      StringBuffer message = new StringBuffer();
      for (WatchDogCheckTask task : taskList) {
        message.append("Time: ").append(task.scheduledExecutionTime()).append("..... ").append(task.getTaskUrl())
            .append('\n');
        task.cancel();
      }
      Mailer.sendMail("Admin", "WARNING PageWatch exceeded 125 tasks", message.toString());
    }
  }

  /**
   * Verify duration of each task, if running longer than 15 minutes then report
   * and kill that task
   */
  private void checkStuckTasks() {
    for (WatchDogCheckTask task : taskList) {
      if (System.currentTimeMillis() - task.scheduledExecutionTime() >= FIFTEEN_MINUTES_IN_MILLISECONDS) {
        LOGGER.trace("WARNING task is running for more than 15 minutes: " + task.getTaskUrl());
        Mailer.sendMail("Admin", "WARNING task is running for more than 15 minutes", task.getTaskUrl());
        task.cancel();
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
  public String getLastrun() {
    return lastRun;
  }

}
