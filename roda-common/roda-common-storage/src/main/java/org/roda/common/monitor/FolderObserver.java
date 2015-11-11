package org.roda.common.monitor;

import java.nio.file.Path;

public interface FolderObserver {
  public void pathAdded(Path basePath, Path createdPath);

  public void pathModified(Path basePath, Path createdPath);

  public void pathDeleted(Path basePath, Path createdPath);
}
