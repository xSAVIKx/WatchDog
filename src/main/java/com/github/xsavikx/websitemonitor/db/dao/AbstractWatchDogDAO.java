package com.github.xsavikx.websitemonitor.db.dao;

import java.util.List;

import javax.sql.DataSource;

import com.github.xsavikx.websitemonitor.db.DataSourceManager;
import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;

public abstract class AbstractWatchDogDAO implements WatchDogDAO {

  protected static final int MAXIMUM_CHECK_LIST_SIZE = 100;
  protected DataSource dataSource;

  protected boolean checkDataSource() {
    if (dataSource == null) {
      dataSource = DataSourceManager.getDataSource();
    }
    return dataSource != null;
  }

  @Override
  public abstract List<WatchDogCheck> getTasksToCheck();

  @Override
  public abstract void storeResult(WatchDogCheck checkresult);
}
