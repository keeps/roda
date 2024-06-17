package org.roda.core.data.v2.ip.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class TypeOptionsInfo {

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
