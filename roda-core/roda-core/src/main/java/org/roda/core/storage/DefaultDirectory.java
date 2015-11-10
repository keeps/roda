/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the Directory inteface.
 * 
 * @see Directory
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class DefaultDirectory extends AbstractResource implements Directory {

  public DefaultDirectory(StoragePath storagePath, Map<String, Set<String>> metadata) {
    super(storagePath, metadata, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DefaultDirectory [getStoragePath()=");
    builder.append(getStoragePath());
    builder.append(", getMetadata()=");
    builder.append(getMetadata());
    builder.append("]");
    return builder.toString();
  }

}
