/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import org.roda.core.data.v2.ip.StoragePath;

/**
 * Default implementation of the Directory inteface.
 * 
 * @see Directory
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class DefaultDirectory extends AbstractResource implements Directory {
  private static final long serialVersionUID = -7684121817358536688L;

  public DefaultDirectory(StoragePath storagePath) {
    super(storagePath, true);
  }

  @Override
  public String toString() {
    return "DefaultDirectory [isDirectory()=" + isDirectory() + ", getStoragePath()=" + getStoragePath() + "]";
  }

}
