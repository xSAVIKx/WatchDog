/**
 * mail utilities
 */
package com.github.xsavikx.websitemonitor.mailer;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

/**
 * Utility class Mailer containing a send email method. <br>
 * <br>
 * The configuration file context.xml must contain the method to use.<br>
 * <Parameter name="email_method" value="eapps" override="false"/> <Parameter
 * name="fromaddress" value="tennisdata@alldatasets.com" override="false"/>
 * <Parameter name="emailusername" value="tennisdata" override="false"/>
 * <Parameter name="emailpassword" value="tennisdata$" override="false"/> (
 * which is dependent on the server the application is running on )<br>
 * <br>
 * There are 3 known settings (as of April 2009)<br>
 * <br>
 * yahoo = using yahoo as email server (default and used for local testing)<br>
 * eapps = server at eapps hosting<br>
 * 4java = server at 4 java (not tested with this application)<br>
 */
public class Mailer {
  private static final Logger LOGGER = Logger.getLogger(Mailer.class);

  static SimpleEmail simpleEmail = null;
  static String administrator_email = null;
  static boolean email_available;

  /**
   * Initialise mailer by setting parameters. <br>
   * <br>
   */
  public static boolean initialise(String email_method, String fromadress, String emailusername, String emailpassword,
      String administrator) {
    LOGGER.debug("initialise(String, String, String, String, String) - start");

    try {

      simpleEmail = new SimpleEmail();
      administrator_email = administrator;

      if ("yahoo".equals(email_method)) {
        /**
         * Send email using Yahoo
         */
        // set the destination of the email
        simpleEmail.setFrom(fromadress);
        simpleEmail.setAuthentication(emailusername, emailpassword);

        simpleEmail.setHostName("smtp.mail.yahoo.co.uk");
        simpleEmail.setSSLOnConnect(true);
        simpleEmail.setSslSmtpPort("465");
        email_available = true;

      } else if ("4java".equals(email_method)) {
        /**
         * Send email using 4java
         */
        simpleEmail.setFrom(fromadress);
        simpleEmail.setAuthentication(emailusername, emailpassword);

        simpleEmail.setHostName("mail.bettingsherlock.info");
        email_available = true;

      } else if ("eapps".equals(email_method)) {
        /**
         * Send email using eapps
         * 
         */
        simpleEmail.setPopBeforeSmtp(false, "mail.sportsbettingscholars.com", "tennisdata", "tennisdata$");
        simpleEmail.setFrom(fromadress);
        simpleEmail.setHostName("sportsbettingscholars.com");
        email_available = true;

      } else if ("126".equals(email_method)) {
        /**
         * Send email using eapps
         * 
         */

        simpleEmail.setFrom(fromadress);
        simpleEmail.setHostName("mail.magic-sw.com.cn");
        simpleEmail.setAuthentication(emailusername, emailpassword);
        email_available = true;
      } else {
        email_available = false;
      }

    } catch (Exception e) {
      LOGGER.error("initialise(String, String, String, String, String)", e);
      LOGGER.debug("initialise(String, String, String, String, String) - end");
      return false;
    }

    LOGGER.debug("initialise(String, String, String, String, String) - end");
    return true;
  }

  /***************************************************************************
   * Send an email message. <br>
   * <br>
   * If email is not available then the message is added to the log file. <br>
   * 
   * @param to
   * @param subject
   * @param message
   * @return
   */
  public static boolean sendMail(String mailAdress, String subject, String message) {
    LOGGER.debug("sendMail(String, String, String) - start");

    List<String> mailList = new ArrayList<String>();
    if (mailAdress.equals("Admin")) {
      mailList.add(administrator_email);
    } else {
      mailList.add(mailAdress);
    }
    boolean returnboolean = sendMail(mailList, subject, message);
    LOGGER.debug("sendMail(String, String, String) - end");
    return returnboolean;
  }

  public static boolean sendMail(List<String> mailList, String subject, String message) {
    LOGGER.debug("sendMail(List<String>, String, String) - start");

    try {
      if (simpleEmail == null) {
        Mailer.initialise("none", "", "", "", "");
      }
      if (email_available) {
        // new mail list
        List<InternetAddress> to = new ArrayList<InternetAddress>();
        for (String toname : mailList) {
          // System.out.println("adding:"+adressee);
          to.add(new InternetAddress(toname));
        }
        simpleEmail.setTo(to);

        // set the subject of the email
        simpleEmail.setSubject(subject);
        // set the content of the email
        simpleEmail.setMsg(message);
        // send the email
        simpleEmail.send();

      } else {
        /**
         * Simulate email sending by using a LOG file
         */
        LOGGER.info("Email not available: " + subject + ";" + message);
      }
    } catch (Exception e) {
      LOGGER.error("sendMail(List<String>, String, String)", e);

      LOGGER.debug("sendMail(List<String>, String, String) - end");
      return false;
    }

    LOGGER.debug("sendMail(List<String>, String, String) - end");
    return true;
  }
}
