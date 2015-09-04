/**
 * Thread that does the actual verification.
 * This file may need to be split into one file per type of action,
 * website, linkback, content, timestamp
 */
package com.github.xsavikx.websitemonitor.timertasks;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.log4j.Logger;

import com.github.xsavikx.websitemonitor.db.dao.DAOFactory;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;
import com.github.xsavikx.websitemonitor.timertasks.helper.FeatureHelper;

public class WatchDogCheckTask extends TimerTask {
  private static final Logger LOGGER = Logger.getLogger(WatchDogCheckTask.class);
  private boolean running = false;
  private WatchDogCheck watchDogCheck;
  private Timer timeIt;
  private HttpClient client;

  private static final String USER_AGENT = "Mozilla/5.0";

  public WatchDogCheckTask(WatchDogCheck watchDogCheck, Timer timeIt) {
    running = true;
    this.watchDogCheck = watchDogCheck;
    this.timeIt = timeIt;
    client = prepareHttpClient();
  }

  private HttpClient prepareHttpClient() {
    return HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
  }

  @Override
  public void run() {
    LOGGER.debug("run() - start");
    checkResource(watchDogCheck);
    DAOFactory.getInstance().getDAOBySource(watchDogCheck.getSource()).storeResult(watchDogCheck);
    cancel();
    LOGGER.debug("run() - end");
  }

  private void checkResource(WatchDogCheck check) {
    LOGGER.debug("checkResource(WatchDogCheck) - start");
    HttpGet get = prepareGetMethod(check.getUrlToCheck());
    int statusCode = WatchDogCheckExceptions.UNKNOWN_EXCEPTION.getCode();
    String responseText = WatchDogCheckExceptions.UNKNOWN_EXCEPTION.getMessage();
    HttpResponse response = null;
    try {
      response = client.execute(get);
      statusCode = response.getStatusLine().getStatusCode();
      responseText = response.getStatusLine().getReasonPhrase();
      processSpecificFeatures(check, response);
    } catch (HttpHostConnectException e) {
      LOGGER.error("checkResource(WatchDogCheck)", e);
      if (e.getCause() instanceof ConnectException) {
        statusCode = WatchDogCheckExceptions.CONNECTION_TIMEOUT.getCode();
        responseText = WatchDogCheckExceptions.CONNECTION_TIMEOUT.getMessage();
      }
    } catch (UnknownHostException e) {
      LOGGER.error("checkResource(WatchDogCheck)", e);

      statusCode = WatchDogCheckExceptions.UNKNOWN_HOST.getCode();
      responseText = WatchDogCheckExceptions.UNKNOWN_HOST.getMessage();
    } catch (ClientProtocolException e) {
      LOGGER.error("checkResource(WatchDogCheck)", e);

      statusCode = WatchDogCheckExceptions.CLIENT_PROTOCOL_EXCEPTION.getCode();
      responseText = WatchDogCheckExceptions.CLIENT_PROTOCOL_EXCEPTION.getMessage();
    } catch (IOException e) {
      LOGGER.error("checkResource(WatchDogCheck)", e);

      statusCode = WatchDogCheckExceptions.IO_EXCEPTION.getCode();
      responseText = WatchDogCheckExceptions.IO_EXCEPTION.getMessage();
    }

    check.setResponseCode("" + statusCode);
    check.setResponseText(responseText);

    LOGGER.debug("checkResource(WatchDogCheck) - end");
  }

  private HttpGet prepareGetMethod(String urlToCheck) {
    HttpGet get = new HttpGet(urlToCheck);
    get.setHeader("User-Agent", USER_AGENT);
    get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    return get;
  }

  private void processSpecificFeatures(WatchDogCheck watchDogCheck, HttpResponse response) {
    LOGGER.debug("processSpecificFeatures(WatchDogCheck, HttpResponse) - start");
    switch (watchDogCheck.getCheckType()) {
      case TIMESTAMP:
        checkTimestampAvailable(watchDogCheck, response);
        break;
      case WEBSITE:
        // nothing to do now
        break;
      default:
        throw new IllegalArgumentException("Such check type is not yet supported");
    }
    LOGGER.debug("processSpecificFeatures(WatchDogCheck, HttpResponse) - end");
  }

  private void checkTimestampAvailable(WatchDogCheck watchDogCheck, HttpResponse response) {
    LOGGER.debug("checkTimestampAvailable(WatchDogCheck, HttpResponse) - start");

    String pageContent = FeatureHelper.getPageContent(response);
    String timestamp = StringUtils.substringBetween(pageContent, "class=\"watchdog_timestamp\">", "</span>");
    watchDogCheck.setTimestampFound(!FeatureHelper.isOld(timestamp, watchDogCheck.getMaximumAge()));

    LOGGER.debug("checkTimestampAvailable(WatchDogCheck, HttpResponse) - end");
  }

  @Override
  public boolean cancel() {
    boolean result = super.cancel();
    timeIt.cancel();
    setRunning(false);
    return result;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean setit) {
    running = setit;
  }

  public String getTaskUrl() {
    return watchDogCheck.getUrlToCheck();
  }

}
