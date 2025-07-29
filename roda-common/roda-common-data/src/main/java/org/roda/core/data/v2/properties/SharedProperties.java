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
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SharedProperties implements Serializable {
  @Serial
  private static final long serialVersionUID = 7250144627803833741L;

  private Map<String, List<String>> properties;

  public SharedProperties() {
    // empty constructor
  }

  public Map<String, List<String>> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, List<String>> properties) {
    this.properties = properties;
  }
}
