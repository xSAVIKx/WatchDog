package com.github.xsavikx.websitemonitor.db.model;

import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;

/**
 *
 */
public class WatchDogCheck extends AbstractEntity {

  /**
   * DatabaseRoutine source
   */
  private DatabaseRoutine source;
  private int referenceId;
  private WatchDogCheckType checkType;
  private String urlToCheck;
  private String urlToFind;
  private String contentToFind;
  private int maximumAge;

  private String responseCode;
  private String responseText;
  private boolean isReachable;
  private boolean isLinkbackFound;
  private boolean isContentFound;
  private boolean isTimestampFound;

  public DatabaseRoutine getSource() {
    return source;
  }

  public void setSource(DatabaseRoutine source) {
    this.source = source;
  }

  public int getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(int referenceId) {
    this.referenceId = referenceId;
  }

  public WatchDogCheckType getCheckType() {
    return checkType;
  }

  public void setCheckType(WatchDogCheckType checkType) {
    this.checkType = checkType;
  }

  public String getUrlToCheck() {
    return urlToCheck;
  }

  public void setUrlToCheck(String urlToCheck) {
    this.urlToCheck = urlToCheck;
  }

  public String getUrlToFind() {
    return urlToFind;
  }

  public void setUrlToFind(String urlToFind) {
    this.urlToFind = urlToFind;
  }

  public String getContentToFind() {
    return contentToFind;
  }

  public void setContentToFind(String contentToFind) {
    this.contentToFind = contentToFind;
  }

  public int getMaximumAge() {
    return maximumAge;
  }

  public void setMaximumAge(int maximumAge) {
    this.maximumAge = maximumAge;
  }

  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public boolean isReachable() {
    return isReachable;
  }

  public void setReachable(boolean isReachable) {
    this.isReachable = isReachable;
  }

  public boolean isLinkbackFound() {
    return isLinkbackFound;
  }

  public void setLinkbackFound(boolean isLinkbackFound) {
    this.isLinkbackFound = isLinkbackFound;
  }

  public boolean isContentFound() {
    return isContentFound;
  }

  public void setContentFound(boolean isContentFound) {
    this.isContentFound = isContentFound;
  }

  public boolean isTimestampFound() {
    return isTimestampFound;
  }

  public void setTimestampFound(boolean isTimestampFound) {
    this.isTimestampFound = isTimestampFound;
  }

}
