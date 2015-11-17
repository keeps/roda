package org.roda.core.common.monitor;

import java.nio.file.Path;

public interface FolderObserver {
  public void pathAdded(Path basePath, Path createdPath, boolean addChildren);

  public void pathModified(Path basePath, Path createdPath, boolean modifyChildren);

  public void pathDeleted(Path basePath, Path createdPath);
}
