/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.properties;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DropdownPluginParameterItems implements Serializable {

  @Serial
  private static final long serialVersionUID = 7674597493405842939L;

  private Set<DropdownPluginParameterItem> items;

  public DropdownPluginParameterItems() {
    items = new HashSet<>();
  }

  public Set<DropdownPluginParameterItem> getItems() {
    return items;
  }

  public void setItems(Set<DropdownPluginParameterItem> items) {
    this.items = items;
  }

  public boolean addObject(DropdownPluginParameterItem item) {
    return this.items.add(item);
  }

  public boolean addObjects(Set<DropdownPluginParameterItem> list) {
    return this.items.addAll(list);
  }
}
