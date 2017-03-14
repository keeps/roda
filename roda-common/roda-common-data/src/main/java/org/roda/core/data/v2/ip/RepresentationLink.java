/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
