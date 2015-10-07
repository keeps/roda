package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class ControlAccesses implements EadCValue, Serializable {

  private static final long serialVersionUID = -9015575690604456375L;

  private ControlAccess[] controlaccesses = null;

  /**
   * Constructs a new empty {@link ControlAccesses}
   * */
  public ControlAccesses() {

  }

  /**
   * Constructs a new {@link ControlAccesses} using provided parameters
   * 
   * @param controlaccesses
   */
  public ControlAccesses(ControlAccess[] controlaccesses) {
    this.controlaccesses = controlaccesses;
  }

  public ControlAccess[] getControlaccesses() {
    return controlaccesses;
  }

  public void setControlaccesses(ControlAccess[] controlaccesses) {
    this.controlaccesses = controlaccesses;
  }

  @Override
  public String toString() {
    return "ControlAccesses [controlaccesses=" + Arrays.toString(controlaccesses) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(controlaccesses);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ControlAccesses)) {
      return false;
    }
    ControlAccesses other = (ControlAccesses) obj;
    if (!Arrays.equals(controlaccesses, other.controlaccesses)) {
      return false;
    }
    return true;
  }

}
