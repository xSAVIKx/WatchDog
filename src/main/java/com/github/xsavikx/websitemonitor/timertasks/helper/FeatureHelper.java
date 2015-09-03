package com.github.xsavikx.websitemonitor.timertasks.helper;

import java.io.IOException;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class FeatureHelper {
  private static final Logger LOGGER = Logger.getLogger(FeatureHelper.class);

  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormat.forPattern(TIMESTAMP_FORMAT)
      .withZoneUTC();

  private FeatureHelper() {
    // private constructor
  }

  /**
   * Get current time in "yyyy-MM-dd HH:mm:ss" format with UTC timezone
   * 
   * @return formated current time
   */
  public static String getCurrentDateTime() {
    return new DateTime().toString(TIMESTAMP_FORMATTER);
  }

  /**
   * Check if given timestamp if older than current time + maximumAge
   * 
   * @param timestamp
   *          - timestamp to check
   * @param maximumAge
   *          - maximum age of timestamp in seconds
   * @return true if timestamp is older than current time + maximumAge
   */
  public static boolean isOld(String timestamp, int maximumAge) {
    LOGGER.debug("isOld(String, int) - start");
    DateTime timestampDateTime = DateTime.parse(timestamp, TIMESTAMP_FORMATTER);
    DateTime now = DateTime.now(DateTimeZone.UTC);
    long interval = (now.getMillis() - timestampDateTime.getMillis()) / 1000;
    boolean returnboolean = interval > maximumAge;
    LOGGER.debug("isOld(String, int) - end");

    return returnboolean;
  }

  /**
   * Retrieve page content from response
   * 
   * @param response
   *          response to retrieve content from
   * @return page content as String or empty string if any error occured
   */
  public static String getPageContent(HttpResponse response) {
    LOGGER.debug("getPageContent(HttpResponse) - start");
    try {
      String returnString = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
      LOGGER.debug("getPageContent(HttpResponse) - end");
      return returnString;
    } catch (ParseException | IOException e) {
      LOGGER.error("getPageContent(HttpResponse)", e);
    }
    LOGGER.debug("getPageContent(HttpResponse) - end");
    return "";
  }
}
