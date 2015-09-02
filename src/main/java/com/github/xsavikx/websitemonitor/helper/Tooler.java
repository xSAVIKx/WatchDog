/**
 * utilities
 */
package com.github.xsavikx.websitemonitor.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provide a set of method to other class
 * 
 * @author : James
 * @version : July 2009
 */
public class Tooler {
  public static String getGMT(Date paramDate) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

      return sdf.format(paramDate);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static Date getDateFromString(String dateString) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try {
      return sdf.parse(dateString);
    } catch (ParseException e) {
      return null;
    }

  }

  public static String getURLFromString(String str) {
    String urlStr = "";
    int startPos = str.toUpperCase().indexOf("HTTP://");
    int endPos = 0;

    if (startPos != -1) {
      for (int i = startPos; i < str.length(); i++) {
        char currChar = str.charAt(i);
        if (currChar == '"') {
          endPos = i;
        }
      }

      urlStr = str.substring(startPos, endPos);
    }
    return urlStr;
  }

  /**
   * Check the given string contain the link
   * 
   * @param source
   * @param link
   * @return
   */
  public static boolean containLink(String source, String link) {

    boolean haveLink = false;

    String regexURL = "<a href=\"" + link + "\">.*?/a>";
    Pattern pt = Pattern.compile(regexURL);
    Matcher mt = pt.matcher(source);

    if (mt.find()) {
      haveLink = true;
    }
    return haveLink;
  }

  /*
   * Analyse the given string and get the part string between the two other
   * strings
   */
  public static String getPartString(String orginalString, String startString, String endString) {
    String resultString = "";

    try {

      String[] tempStringArray = parseSplitStr1(orginalString, startString);

      resultString = tempStringArray[1];

      tempStringArray = parseSplitStr1(resultString, endString);

      resultString = tempStringArray[0];

      return resultString;
    } catch (Exception e) {
      return "";
    }

  }

  public static String[] parseSplitStr1(String splitString, String splitStr) {
    String[] strArray = null;
    strArray = splitString.split(splitStr);
    return strArray;
  }

}
