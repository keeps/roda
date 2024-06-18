package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class TypeOptionsInfo implements Serializable {

  @Serial
  private static final long serialVersionUID = 8624436299370640300L;

  private boolean isControlled;
  private List<String> types;

  public TypeOptionsInfo() {
    // do nothing
  }

  public TypeOptionsInfo(boolean isControlled, List<String> types) {
    this.isControlled = isControlled;
    this.types = types;
  }

  public boolean isControlled() {
    return isControlled;
  }

  public void setControlled(boolean controlled) {
    isControlled = controlled;
  }

  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }
}
