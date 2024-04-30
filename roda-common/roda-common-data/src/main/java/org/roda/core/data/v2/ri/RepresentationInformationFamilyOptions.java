package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationFamilyOptions implements Serializable {
  @Serial
  private static final long serialVersionUID = 1463314655573249956L;

  private Map<String, String> options = new HashMap<>();

  public RepresentationInformationFamilyOptions() {
    // empty constructor
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
