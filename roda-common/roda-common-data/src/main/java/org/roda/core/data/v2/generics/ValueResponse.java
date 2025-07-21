package org.roda.core.data.v2.generics;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ValueResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = -560864427759571057L;

  private String value;

  public ValueResponse() {
    // empty constructor
  }

  public ValueResponse(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}