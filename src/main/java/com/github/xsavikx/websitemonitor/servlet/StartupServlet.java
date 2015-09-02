/**
 * Configured by web.xml to start running as soon as the server starts up.
 */
package com.github.xsavikx.websitemonitor.servlet;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.constants.ApplicationConstants;
import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;
import com.github.xsavikx.websitemonitor.mailer.Mailer;
import com.github.xsavikx.websitemonitor.timertasks.MasterTask;
import com.github.xsavikx.websitemonitor.timertasks.MonitorTask;

/**
 * This is the applications main servlet that gets started automatically.<br>
 * 
 * @author Erik Bogaert
 * @version July 2009
 */
public class StartupServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
  private static final Logger LOGGER = Logger.getLogger(StartupServlet.class);

  static final long serialVersionUID = 1L;

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

  private void initDatabaseRoutines() {
    LOGGER.debug("initDatabaseRoutines() - start");

    LOGGER.info("Initializing database routines");
    try {
      Context initialContext = new InitialContext();
      try {
        for (DatabaseRoutine r : DatabaseRoutine.values()) {
          initialContext.lookup("java:comp/env/databaseRoutine/" + r.getSource());
        }
      } catch (Exception e) {
        // ignore
      }
    } catch (NamingException e1) {
      // ignore
    }

    LOGGER.debug("initDatabaseRoutines() - end");
  }

  private void initInternalClasses() {
    LOGGER.debug("initInternalClasses() - start");
    LOGGER.info("Initializing internal classes");
    initApplicationConstants(getServletContext());
    initMailer();
    initDatabaseRoutines();

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
    ApplicationConstants.delayBetweenRecord = Integer.valueOf((context.getInitParameter("delayBetweenRecord")));
    ApplicationConstants.mainTaskInterval = Integer.valueOf((context.getInitParameter("mainTaskInterval")));

    LOGGER.debug("initApplicationConstants(ServletContext) - end");
  }

}
