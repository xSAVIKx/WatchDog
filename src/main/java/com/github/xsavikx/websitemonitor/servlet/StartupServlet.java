/**
 * Configured by web.xml to start running as soon as the server starts up.
 */
package com.github.xsavikx.websitemonitor.servlet;

import java.util.Timer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
  static final long serialVersionUID = 1L;

  /**
   * Servlet init() method , kicks off all permanent timertasks.
   * 
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    initInternalClasses();

    // notify administrator application has started
    Mailer.sendMail("Admin", "Starting PageWatch application...", "Starting PageWatch application");
    Timer timeIt = new Timer();

    /**
     * Start monitor thread to run typically once every 5 minutes checks the
     * number of threads running as well as their duration.
     */
    MonitorTask monitor = new MonitorTask();
    timeIt = new Timer();
    timeIt.schedule(monitor, 0, 300000); // run once every 5 minutes

    /**
     * Start master thread to run typically once every 10 minutes
     */
    // timeIt.schedule(new timertasks.MasterTask(monitor), 10000, (10*1000)); //
    // testing 10 seconds
    timeIt.schedule(new MasterTask(monitor), 10000, ApplicationConstants.mainTaskInterval);
  }

  private void initDatabaseRoutines() {
    try {
      Context initialContext = new InitialContext();
      try {
        for (DatabaseRoutine r : DatabaseRoutine.values()) {
          DatabaseRoutine res = (DatabaseRoutine) initialContext.lookup("java:comp/env/databaseRoutine/"
              + r.getSource());
        }
      } catch (Exception e) {
        // ignore
      }
    } catch (NamingException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }

  private void initInternalClasses() {
    initApplicationConstants(getServletContext());
    initMailer();
    initDatabaseRoutines();
  }

  private void initMailer() {
    Mailer.initialise(ApplicationConstants.emailMethod, ApplicationConstants.fromAdress,
        ApplicationConstants.emailUsername, ApplicationConstants.emailPassword, ApplicationConstants.toAdress);
  }

  private void initApplicationConstants(ServletContext context) {
    ApplicationConstants.emailMethod = context.getInitParameter("email_method");
    ApplicationConstants.toAdress = context.getInitParameter("toadress");
    ApplicationConstants.fromAdress = context.getInitParameter("fromadress");
    ApplicationConstants.emailUsername = context.getInitParameter("emailusername");
    ApplicationConstants.emailPassword = context.getInitParameter("emailpassword");
    ApplicationConstants.delayBetweenRecord = Integer.valueOf((context.getInitParameter("delayBetweenRecord")));
    ApplicationConstants.mainTaskInterval = Integer.valueOf((context.getInitParameter("mainTaskInterval")));
  }

}
