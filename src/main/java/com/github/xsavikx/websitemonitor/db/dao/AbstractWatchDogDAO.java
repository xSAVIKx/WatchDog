package com.github.xsavikx.websitemonitor.db.dao;

import java.util.List;

import javax.sql.DataSource;

import com.github.xsavikx.websitemonitor.db.DataSourceManager;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;
import com.github.xsavikx.websitemonitor.mailer.Mailer;

public abstract class AbstractWatchDogDAO implements WatchDogDAO {

  protected static final int MAXIMUM_CHECK_LIST_SIZE = 100;
  protected DataSource dataSource;
  private static final String MAXIMUM_CHECK_LIST_SIZE_EXCEEDED_MAIL_TITLE_FORMAT = "Check size of list is more than %d elements.";
  private static final String MAXIMUM_CHECK_LIST_SIZE_EXCEEDED_MAIL_MESSAGE_FORMAT = MAXIMUM_CHECK_LIST_SIZE_EXCEEDED_MAIL_TITLE_FORMAT
      + " Actual check list size is: %d elements";

  private static final String RETURN_CODE_CHANGED_MAIL_TITLE = "Return code changed.";
  private static final String RETURN_CODE_CHANGED_MAIL_MESSAGE_FORMAT = "Return code changed from %s to %s.\nMessages changed from %s to %s.";

  protected boolean checkDataSource() {
    if (dataSource == null) {
      dataSource = DataSourceManager.getDataSource();
    }
    return dataSource != null;
  }

  protected void sendMailToAdmin(String subject, String message) {
    Mailer.sendMail("Admin", subject, message);
  }

  protected void sendMaximumCheckListSizeExceeded(List<WatchDogCheck> checkList) {
    sendMailToAdmin(String.format(MAXIMUM_CHECK_LIST_SIZE_EXCEEDED_MAIL_TITLE_FORMAT, MAXIMUM_CHECK_LIST_SIZE),
        String.format(MAXIMUM_CHECK_LIST_SIZE_EXCEEDED_MAIL_MESSAGE_FORMAT, MAXIMUM_CHECK_LIST_SIZE, checkList.size()));
  }

  protected void sendReturnCodeChangedMail(String lastReturnCode, String lastReturnText, String newReturnCode,
      String newReturnText) {
    sendMailToAdmin(RETURN_CODE_CHANGED_MAIL_TITLE, String.format(RETURN_CODE_CHANGED_MAIL_MESSAGE_FORMAT,
        lastReturnCode, newReturnCode, lastReturnText, newReturnText));
  }

  @Override
  public abstract List<WatchDogCheck> getTasksToCheck();

  @Override
  public abstract void storeResult(WatchDogCheck checkresult);
}
