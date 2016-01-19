/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.TransferredResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyObserver implements FolderObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(DummyObserver.class);

  private Set<String> files;
  private Set<String> directories;

  public DummyObserver() throws IOException {
    files = new HashSet<String>();
    directories = new HashSet<String>();
  }

  public Set<String> getFiles() {
    return files;
  }

  public void setFiles(Set<String> files) {
    this.files = files;
  }

  public Set<String> getDirectories() {
    return directories;
  }

  public void setDirectories(Set<String> directories) {
    this.directories = directories;
  }

  @Override
  public void transferredResourceAdded(TransferredResource resource) {
    LOGGER.info("CREATED: " + resource.getFullPath());
    if (resource.isFile()) {
      files.add(resource.getFullPath());
    } else {
      directories.add(resource.getFullPath());
    }
  }

  @Override
  public void transferredResourceModified(TransferredResource resource) {
    LOGGER.info("TR MODIFIED: " + resource.getRelativePath());
  }

  @Override
  public void transferredResourceDeleted(TransferredResource resource) {
    LOGGER.info("TR DELETED: " + resource.getRelativePath());
  }

  @Override
  public void pathDeleted(Path deleted) {
    LOGGER.info("PATH DELETED: " + deleted.toString());
  }

}
