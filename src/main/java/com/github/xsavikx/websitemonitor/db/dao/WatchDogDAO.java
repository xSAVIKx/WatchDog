package com.github.xsavikx.websitemonitor.db.dao;

import java.util.List;

import com.github.xsavikx.websitemonitor.db.model.WatchDogCheck;

public interface WatchDogDAO {
  public List<WatchDogCheck> getTasksToCheck();

  public void storeResult(WatchDogCheck checkresult);
}
