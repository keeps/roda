package org.roda.core.data.v2.generics;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LongResponse implements Serializable {


  @Serial
  private static final long serialVersionUID = 5965085690339865049L;

  private Long result;

  public LongResponse() {
    // empty constructor
  }

  public LongResponse(Long result) {
    this.result = result;
  }

  public Long getResult() {
    return result;
  }

  public void setResult(Long result) {
    this.result = result;
  }
}
