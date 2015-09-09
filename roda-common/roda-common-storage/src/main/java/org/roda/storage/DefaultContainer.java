package org.roda.storage;

import java.util.Map;
import java.util.Set;

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
 * */
public class DefaultContainer extends AbstractEntity implements Container {

  public DefaultContainer(StoragePath storagePath, Map<String, Set<String>> metadata) {
    super(storagePath, metadata);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DefaultContainer [getStoragePath()=");
    builder.append(getStoragePath());
    builder.append(", getMetadata()=");
    builder.append(getMetadata());
    builder.append("]");
    return builder.toString();
  }

}
