/**
 * Configured by web.xml to start running as soon as the server starts up.
 */
package com.github.xsavikx.websitemonitor.servlet;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.constants.ApplicationConstants;
import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;
import com.github.xsavikx.websitemonitor.mailer.Mailer;
import com.github.xsavikx.websitemonitor.timertasks.MasterTask;
import com.github.xsavikx.websitemonitor.timertasks.MonitorTask;

public class StartupServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
  private static final long serialVersionUID = -6387768385216929007L;
  private static final Logger LOGGER = Logger.getLogger(StartupServlet.class);

  /**
   * Servlet init() method , kicks off all permanent timertasks.
   * 
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    LOGGER.debug("init() - start");

    initInternalClasses();

    // notify administrator application has started
    LOGGER.info("Sending e-mail to administrator about starting WatchDog application");
    Mailer.sendMail("Admin", "Starting WatchDog application...", "Starting WatchDog application");
    Timer timeIt = new Timer();

    /**
     * Start monitor thread to run typically once every 5 minutes checks the
     * number of threads running as well as their duration.
     */
    MonitorTask monitor = new MonitorTask();
    timeIt = new Timer();
    long threeSeconds = TimeUnit.SECONDS.toMillis(3);
    long fiveMinutes = TimeUnit.MINUTES.toMillis(3);
    timeIt.schedule(monitor, threeSeconds, fiveMinutes);

    /**
     * Start master thread to run typically once every 10 minutes
     */
    // timeIt.schedule(new timertasks.MasterTask(monitor), 10000, (10*1000)); //
    // testing 10 seconds
    timeIt.schedule(new MasterTask(monitor), TimeUnit.SECONDS.toMillis(5), ApplicationConstants.mainTaskInterval);

    LOGGER.debug("init() - end");
  }

  private void initDatabaseRoutines(ServletContext context) {
    LOGGER.debug("initDatabaseRoutines() - start");

    LOGGER.info("Initializing database routines");
    for (DatabaseRoutine r : DatabaseRoutine.values()) {
      String value = context.getInitParameter(r.getSource());
      if (DatabaseRoutine.IS_ON_VALUE_PATTERN.equalsIgnoreCase(value)) {
        r.setOn(true);
      }
    }

    LOGGER.debug("initDatabaseRoutines() - end");
  }

  private void initInternalClasses() {
    LOGGER.debug("initInternalClasses() - start");
    LOGGER.info("Initializing internal classes");
    ServletContext context = getServletContext();
    initApplicationConstants(context);
    initMailer();
    initDatabaseRoutines(context);

    LOGGER.debug("initInternalClasses() - end");
  }

  private void initMailer() {
    LOGGER.debug("initMailer() - start");

    LOGGER.info("Initializing Mailer");
    Mailer.initialise(ApplicationConstants.emailMethod, ApplicationConstants.fromAdress,
        ApplicationConstants.emailUsername, ApplicationConstants.emailPassword, ApplicationConstants.toAdress);

    LOGGER.debug("initMailer() - end");
  }

  private void initApplicationConstants(ServletContext context) {
    LOGGER.debug("initApplicationConstants(ServletContext) - start");

    LOGGER.info("Initializing application constants");
    ApplicationConstants.emailMethod = context.getInitParameter("email_method");
    ApplicationConstants.toAdress = context.getInitParameter("toadress");
    ApplicationConstants.fromAdress = context.getInitParameter("fromadress");
    ApplicationConstants.emailUsername = context.getInitParameter("emailusername");
    ApplicationConstants.emailPassword = context.getInitParameter("emailpassword");
    String delayBetweenRecord = context.getInitParameter("delayBetweenRecord");
    if (NumberUtils.isDigits(delayBetweenRecord)) {
      ApplicationConstants.delayBetweenRecord = Integer.valueOf(delayBetweenRecord);
    }
    String mainTaskInterval = context.getInitParameter("mainTaskInterval");
    if (NumberUtils.isDigits(mainTaskInterval)) {
      ApplicationConstants.mainTaskInterval = Integer.valueOf(mainTaskInterval);
    }
    LOGGER.debug("initApplicationConstants(ServletContext) - end");
  }
}
