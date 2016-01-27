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
 * Default implementation of the {@link Container} interface.
 *
 * <p>
 * <i>A container is a resource that represents intellectual entities and can
 * also be used to aggregate other resources. Containers may container other
 * containers or binaries and their metadata.</i>
 * </p>
 *
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * 
 * @see Container
 *
 */
public class DefaultContainer extends AbstractEntity implements Container {

  public DefaultContainer(StoragePath storagePath) {
    super(storagePath);
  }

  @Override
  public String toString() {
    return "DefaultContainer [getStoragePath()=" + getStoragePath() + "]";
  }

}
