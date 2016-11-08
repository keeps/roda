package org.roda.core.data.v2.ip;

import java.io.Serializable;

public class RepresentationLink extends AIPLink implements Serializable {

  private static final long serialVersionUID = -1203642066028643508L;
  private String representationId;

  public RepresentationLink() {
    super();
  }

  public RepresentationLink(String aipId, String representationId) {
    super(aipId);
    this.representationId = representationId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  @Override
  public String toString() {
    return "RepresentationLink [representationId=" + representationId + "]";
  }
}
