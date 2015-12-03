package org.roda.core.common.monitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

import org.roda.core.data.v2.TransferredResource;

public class DummyObserver implements FolderObserver {
  HashSet<String> files;
  HashSet<String> directories;

  public DummyObserver() throws IOException {
    files = new HashSet<String>();
    directories = new HashSet<String>();
  }

  public HashSet<String> getFiles() {
    return files;
  }

  public void setFiles(HashSet<String> files) {
    this.files = files;
  }

  public HashSet<String> getDirectories() {
    return directories;
  }

  public void setDirectories(HashSet<String> directories) {
    this.directories = directories;
  }

  @Override
  public void transferredResourceAdded(TransferredResource resource, boolean commit) {
    System.out.println("CREATED: " + resource.getFullPath());
    if (resource.isFile()) {
      files.add(resource.getFullPath());
    } else {
      directories.add(resource.getFullPath());
    }

  }

  @Override
  public void transferredResourceAdded(TransferredResource resource) {
    System.out.println("CREATED: " + resource.getFullPath());
    if (resource.isFile()) {
      files.add(resource.getFullPath());
    } else {
      directories.add(resource.getFullPath());
    }
  }

  @Override
  public void transferredResourceModified(TransferredResource resource) {
    System.out.println("TR MODIFIED: " + resource.getRelativePath());

  }

  @Override
  public void transferredResourceDeleted(TransferredResource resource) {
    System.out.println("TR DELETED: " + resource.getRelativePath());

  }

  @Override
  public void pathDeleted(Path deleted) {
    System.out.println("PATH DELETED: " + deleted.toString());

  }

}
