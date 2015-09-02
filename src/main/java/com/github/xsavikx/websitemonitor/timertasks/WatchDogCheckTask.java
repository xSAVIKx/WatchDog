/**
 * Thread that does the actual verification.
 * This file may need to be split into one file per type of action,
 * website, linkback, content, timestamp
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.constants.ApplicationConstants;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;
import com.github.xsavikx.websitemonitor.helper.Tooler;
import com.github.xsavikx.websitemonitor.mailer.Mailer;

/**
 * Task to check a webpage.
 */
public class WatchDogCheckTask extends TimerTask {
  private static final Logger LOGGER = Logger.getLogger(WatchDogCheckTask.class);

  /**
   * Define the result code for the check result
   */
  private final byte OK_RESULT = 0;
  private final byte OK_TMPCHECKRESULT = 1;

  private final byte ERROR_INVALIDATEURL = -1;
  private final byte ERROR_URLCONNECT = -2;
  private final byte ERROR_NOTCONTAINSTRING = -3;
  private final byte ERROR_NOTCONTAINLINK = -4;
  private final byte ERROR_OLDPAGETIMESTAMP = -5;
  private final byte ERROR_DBNOTSETTIMESTAMP = -6;
  private final byte ERROR_UNDEFINED = -7;

  /**
   * Define the error action
   */
  private final byte ERRORACTION_NOTHING = 0;
  private final byte ERRORACTION_MAILADMIN = 1;
  private final byte ERRORACTION_LOG = 2;
  private final byte ERRORACTION_MAILLIST = 3;

  /**
   * Define the check type
   */
  private final byte CHECKTYPE_URL = 1;
  private final byte CHECKTYPE_PAGECONTENT = 2;
  private final byte CHECKTYPE_PAGELINK = 3;
  private final byte CHECKTYPE_PAGETIMESTAMP = 4;

  // private Date threadStartTime = new Date();
  private boolean running = false;
  private WatchDogCheck watchDogCheck;
  public Timer timeIt;
  private HttpClient client;

  private static final String USER_AGENT = "Mozilla/5.0";

  // int runcounter = 0;

  public WatchDogCheckTask(WatchDogCheck watchDogCheck, Timer timeIt) {
    running = true;
    this.watchDogCheck = watchDogCheck;
    this.timeIt = timeIt;
    client = HttpClients.createMinimal();
  }

  @Override
  public void run() {
    try {
      /**
       * check if the page is reachable, and if the expected content can be
       * found, returns an integer value of 0 for OK or any number of error
       * values
       */
      int checkResult = ERROR_UNDEFINED;
      int checkType = CHECKTYPE_URL;
      checkResult = checkResource(watchDogCheck.getUrlToCheck(), watchDogCheck.getContentToFind(), checkType);
      // System.out.println("Check result : "+checkResult);
      String currentStatus = checkResult == OK_RESULT ? "OK" : "ERROR";

      /**
       * write check result to database, either OK or ERROR if the status goes
       * from OK to ERROR then check error action ( also if ERROR without any
       * previous record of a check )
       */
      boolean error_action = false;
//      if (ApplicationConstants.databaseRoutine.equals("pagewatch")) {
        // error_action = dblayer.DBAccessor.storeResult(watchDogCheck,
        // currentStatus);
//      } else if (ApplicationConstants.databaseRoutine.equals("punterindex")) {
        // error_action = dblayer.DBpunterindex.storeResult(watchDogCheck,
        // currentStatus);
//      } else {
        // error, unknown databaseroutine
//        LOGGER.fatal("invalid databaseroutine parameter:" + ApplicationConstants.databaseRoutine);
//        return;
//      }
      // System.out.println("Error action : "+error_action);

      /**
       * if there is an error after being OK or unknown, then take the error
       * action for this page
       */
      if (error_action) {

        // to be expanded
        String errorMessage = watchDogCheck.getUrlToCheck() + ":" + "error reason:" + checkResult + "\n";

        // if (watchDogCheck.getErrorAction() == ERRORACTION_NOTHING) {
        /**
         * do nothing
         */
        // } else if (watchDogCheck.getErrorAction() == ERRORACTION_LOG) {
        /**
         * make log file entry
         */
        LOGGER.info(errorMessage);
        // } else if (watchDogCheck.getErrorAction() == ERRORACTION_MAILADMIN) {
        /**
         * Send notice to administrator TODO to be expanded with page as file
         * attachments
         */
        Mailer.sendMail("Admin", "errorCheck " + watchDogCheck.getUrlToCheck(), watchDogCheck.getUrlToCheck()
            + " is checked with error: " + checkResult);
        // } else if (watchDogCheck.getErrorAction() == ERRORACTION_MAILLIST) {
        /**
         * Send emails to everyone on the email list for this page TODO to be
         * expanded with page as file attachments TODO possibly change into
         * mailing the administrator with the people on the email list as blind
         * copies
         */
        List<String> mailList = new ArrayList<String>();
//        if (ApplicationConstants.databaseRoutine.equals("pagewatch")) {
          // mailList = dblayer.DBAccessor.getMails(watchDogCheck.getPageID());
//        } else if (ApplicationConstants.databaseRoutine.equals("punterindex")) {
          // should not occur as punterindex is fixed to no-action, maybe in
          // future
          // mailList =
          // dblayer.DBpunterindex.getMails(watchDogCheck.getPageID());
        }
//        LOGGER.debug("maillist=" + mailList.size());
        // to be expanded
//        Mailer.sendMail(mailList, "errorCheck " + watchDogCheck.getUrlToCheck(), watchDogCheck.getUrlToCheck()
//            + " is checked with error: " + checkResult);
//      } else {
        /**
         * unknown error action code
         */
//        Mailer.sendMail("Admin", "PageWatch error: unknown error action", "unknown error action");
//      }
      // }

      // mark the state as not running so the monitor thread can remove the task
      // from its list
      running = false;
      // cancel timer this task is running on,
      // makes it available for garbage collection when all references are
      // removed
      // ( must remove from the monitor list to be free )
      timeIt.cancel();
      // cancel running task so it never runs again
      cancel();

      // if ( runcounter > 0 ) {
      // System.out.println("Should never occur, more than one run of a task..................................................."+runcounter);
      // }
      // runcounter++;

    } catch (Exception e) {
      // nothing yet
    }

  }

  /**
   * Check the given content
   * 
   * @param strUrl
   * @return 0-----> ok 1-----> is a temp value,it stands find the given string
   *         and last to update it to zero -1----> url is invalid -2----> can
   *         not connect the url -3----> can not contain the given string
   *         -4----> can not find the given link -5----> pagestamp is old
   *         -6----> when check the pagestamp but the pagestamp is not set
   *         correctly in database
   */
  public synchronized int checkResource(String pageURL, String checkContent, int checkType) {

    int checkResult = OK_RESULT;
    HttpURLConnection URLConn = null;
    InputStream in = null;

    try {

      URL url = new URL(pageURL);

      /*
       * Check the given url exists, because sometimes the not exists url will
       * be redirect some search engine website So need get the contentLength to
       * check exists or not
       */
      HttpGet get = new HttpGet(pageURL);
      get.setHeader("User-Agent", USER_AGENT);
      get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

      HttpResponse response = client.execute(get);
      int statusCode = response.getStatusLine().getStatusCode();
      String reasonPhrase = response.getStatusLine().getReasonPhrase();
      URL tempURL = new URL(url.getProtocol() + "://" + url.getHost());

      URLConn = (HttpURLConnection) tempURL.openConnection();

      int contentLength = URLConn.getContentLength();

      if (contentLength == 0 || contentLength == -1) {
        checkResult = ERROR_URLCONNECT;
        return checkResult;
      }

      /*
       * If exists then get the content stream and check other content
       */

      in = url.openStream();

      InputStreamReader isr = new InputStreamReader(in);

      BufferedReader br = new BufferedReader(isr);

      /*
       * This part is for check the given string within the page url
       */
      if (checkType != CHECKTYPE_URL) {
        String str = null;

        while ((str = br.readLine()) != null) {

          /**
           * Sometimes the content contain the meta and has the url redirect
           * information if the meta contain the url then redirect the meta url
           * and get the data
           */
          if (str.trim().toUpperCase().startsWith("<META")) {
            // if the page source contain meta and check meta
            // contain the url redirect whether or not
            String redirectURL = Tooler.getURLFromString(str);

            // if contain then redirect the url and recheck
            if (redirectURL != "") {
              checkResult = checkResource(redirectURL, checkContent, checkType);
              return checkResult;
            }
          }

          // if the line contain the given text or pageLink
          if (checkType == CHECKTYPE_PAGECONTENT && str.contains(checkContent)) {

            checkResult = OK_TMPCHECKRESULT;
            break;
          }

          /*
           * Actually the best way to validate the page link to use the regx
           * ruler but now there is some problems to construct the ruler I will
           * fixed this problem
           */
          if (checkType == CHECKTYPE_PAGELINK) {
            /*
             * if(Tooler.containLink(str, checkContent)){ checkResult=1; break;
             * }
             */
            LOGGER.debug("Checking link:" + checkContent);

            if (str.contains(checkContent)) {
              checkResult = OK_TMPCHECKRESULT;
              break;
            }
          }

          /*
           * get the pagestamp from the page and compare with the GMT Time
           */

          if (checkType == CHECKTYPE_PAGETIMESTAMP) {
            String pageTimestamp = "";
            pageTimestamp = Tooler.getPartString(str, "class=\"pagewatch_timestamp\">", "</span>");

            if (!pageTimestamp.equals("")) {

              Date pageDatetime = Tooler.getDateFromString(pageTimestamp);
              Date currentGMPDate = Tooler.getDateFromString(Tooler.getGMT(new Date()));

              Calendar c1 = Calendar.getInstance();
              c1.setTime(pageDatetime);

              Calendar c2 = Calendar.getInstance();
              c2.setTime(currentGMPDate);

              long interval = (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000;

              try {
                long intervalValidValue = Long.valueOf(checkContent);
                if (interval < intervalValidValue) {
                  checkResult = OK_TMPCHECKRESULT;
                }
              } catch (Exception e) {
                checkResult = ERROR_DBNOTSETTIMESTAMP;
              }

              break;

            }

          }

        }
      }

      if (checkResult != OK_TMPCHECKRESULT && checkType == CHECKTYPE_PAGECONTENT) {
        checkResult = ERROR_NOTCONTAINSTRING;
      }

      if (checkResult != OK_TMPCHECKRESULT && checkType == CHECKTYPE_PAGELINK) {
        checkResult = ERROR_NOTCONTAINLINK;
      }

      if (checkResult != OK_TMPCHECKRESULT && checkType == CHECKTYPE_PAGETIMESTAMP) {
        checkResult = ERROR_OLDPAGETIMESTAMP;
      }

      if (checkResult == OK_TMPCHECKRESULT) {
        checkResult = OK_RESULT;
      }

    } catch (MalformedURLException e) {
      checkResult = ERROR_INVALIDATEURL;
    } catch (IOException e) {
      // e.printStackTrace();
      checkResult = ERROR_URLCONNECT;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {

      try {
        if (URLConn != null) {
          URLConn.disconnect();
        }

        if (in != null) {
          in.close();
        }

      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    return checkResult;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean setit) {
    running = setit;
  }

  public String getName() {
    return watchDogCheck.getUrlToCheck();
  }
  // public Date getThreadStartTime() {
  // return threadStartTime;
  // }

}
