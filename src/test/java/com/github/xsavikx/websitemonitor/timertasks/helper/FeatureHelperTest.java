package com.github.xsavikx.websitemonitor.timertasks.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTimeUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FeatureHelperTest {
  private static final long JANUARY_2_2001_12_00_00_IN_MILLIS = 978436800000L;

  @BeforeClass
  public static void setUpClass() {
    DateTimeUtils.setCurrentMillisFixed(JANUARY_2_2001_12_00_00_IN_MILLIS);
  }

  @AfterClass
  public static void tearDownClass() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void testGetCurrentDateTime() {
    String expected = "2001-01-02 12:00:00";
    String actual = FeatureHelper.getCurrentDateTime();
    assertEquals("Should return January 2, 2001 12:00:00 PM", expected, actual);
  }

  @Test
  public void testIsOldWithNewTimestamp() {
    int maximumAge = 60;
    String timestamp = "2001-01-02 12:00:30";
    assertFalse("Timestamp should NOT be old", FeatureHelper.isOld(timestamp, maximumAge));
  }

  @Test
  public void testIsOldWithOldTimestamp() {
    int maximumAge = 60;
    String timestamp = "2001-01-02 11:58:59";
    assertTrue("Timestamp should be old", FeatureHelper.isOld(timestamp, maximumAge));
  }

}
