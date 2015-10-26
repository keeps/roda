/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

public class SimpleRepresentationPreservationMetadata extends SimplePreservationMetadata {
  private static final long serialVersionUID = 3107262064744644722L;
  public static final String TYPE = "representation";
  private String representationObjectID = null;

  public SimpleRepresentationPreservationMetadata() {

  }

  public SimpleRepresentationPreservationMetadata(RepresentationPreservationObject rpo) {
    this.representationObjectID = rpo.getRepresentationObjectID();
  }

  public SimpleRepresentationPreservationMetadata(SimpleRepresentationPreservationMetadata simple) {
    this.setAipId(simple.getAipId());
    this.setCreatedDate(simple.getCreatedDate());
    this.setFileId(simple.getFileId());
    this.setId(simple.getId());
    this.setLabel(simple.getLabel());
    this.setLastModifiedDate(simple.getLastModifiedDate());
    this.setModel(simple.getModel());
    this.setRepresentationId(simple.getRepresentationId());
    this.setRepresentationObjectID(simple.getRepresentationObjectID());
    this.setState(simple.getState());
    this.setType(simple.getType());
  }

  public String getRepresentationObjectID() {
    return representationObjectID;
  }

  public void setRepresentationObjectID(String representationObjectID) {
    this.representationObjectID = representationObjectID;
  }

  public String getType() {
    return TYPE;
  }

}
