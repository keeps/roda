/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import org.roda.core.data.v2.ip.StoragePath;

public class AbstractEntity implements Entity {
  private static final long serialVersionUID = 208895186797132039L;

  private StoragePath storagePath;

  public AbstractEntity(StoragePath storagePath) {
    super();
    this.storagePath = storagePath;
  }

  /**
   * @return the storagePath
   */
  @Override
  public StoragePath getStoragePath() {
    return storagePath;
  }

  /**
   * @param storagePath
   *          the storagePath to set
   */
  public void setStoragePath(StoragePath storagePath) {
    this.storagePath = storagePath;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractEntity other = (AbstractEntity) obj;
    if (storagePath == null) {
      if (other.storagePath != null)
        return false;
    } else if (!storagePath.equals(other.storagePath))
      return false;
    return true;
  }

}
