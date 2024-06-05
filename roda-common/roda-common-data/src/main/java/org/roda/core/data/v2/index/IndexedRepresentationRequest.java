package org.roda.core.data.v2.index;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IndexedRepresentationRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -345956922313791766L;

  private String aipId;
  private String representationId;

  public IndexedRepresentationRequest() {
    // empty constructor
  }

  public IndexedRepresentationRequest(String aipId, String representationId) {
    this.aipId = aipId;
    this.representationId = representationId;
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }
}
