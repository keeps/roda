/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import org.roda.core.data.v2.ip.StoragePath;

public class AbstractResource extends AbstractEntity implements Resource {
  private static final long serialVersionUID = -7138875367059222378L;

  private boolean directory;

  public AbstractResource(StoragePath storagePath, boolean directory) {
    super(storagePath);
    this.directory = directory;
  }

  /**
   * @return the directory
   */
  @Override
  public boolean isDirectory() {
    return directory;
  }

  /**
   * @param directory
   *          the directory to set
   */
  public void setDirectory(boolean directory) {
    this.directory = directory;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (directory ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractResource other = (AbstractResource) obj;
    if (directory != other.directory) {
      return false;
    }
    return true;
  }

}
