/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConfiguredDescriptiveMetadataList implements Serializable {
  @Serial
  private static final long serialVersionUID = 2412293709745299277L;

  private List<ConfiguredDescriptiveMetadata> list = new ArrayList<>();

  public ConfiguredDescriptiveMetadataList() {
    // empty constructor
  }

  public List<ConfiguredDescriptiveMetadata> getList() {
    return list;
  }

  public void setList(List<ConfiguredDescriptiveMetadata> list) {
    this.list = list;
  }

  public boolean addObject(ConfiguredDescriptiveMetadata metadata) {
    return this.list.add(metadata);
  }
}
