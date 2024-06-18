package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConfiguredDescriptiveMetadataList implements Serializable {
  @Serial
  private static final long serialVersionUID = 2412293709745299277L;

  private Set<ConfiguredDescriptiveMetadata> list = new HashSet<>();

  public ConfiguredDescriptiveMetadataList() {
    // empty constructor
  }

  public Set<ConfiguredDescriptiveMetadata> getList() {
    return list;
  }

  public void setList(Set<ConfiguredDescriptiveMetadata> list) {
    this.list = list;
  }

  public boolean addObject(ConfiguredDescriptiveMetadata metadata) {
    return this.list.add(metadata);
  }
}
