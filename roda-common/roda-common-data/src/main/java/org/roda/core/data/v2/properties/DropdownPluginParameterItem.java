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

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DropdownPluginParameterItem implements Serializable {
  @Serial
  private static final long serialVersionUID = -1886414999985024868L;

  private String id;
  private String label;

  public DropdownPluginParameterItem(String id, String label) {
    this.id = id;
    this.label = label;
  }

  public DropdownPluginParameterItem() {
    // empty constructor
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
