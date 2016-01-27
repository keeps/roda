package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

public class PreservationLinkingObject implements Serializable {
  public enum LINKING_OBJECT_TYPE {
    SOURCE, OUTCOME
  }
  
  private static final long serialVersionUID = -3796945190331813821L;
  private String role;
  private String title;
  private String type;
  private String identifierType;
  private String identifierValue;
  private LINKING_OBJECT_TYPE objectType;
  
  public PreservationLinkingObject() {
  }
  public String getRole() {
    return role;
  }
  public void setRole(String role) {
    this.role = role;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public LINKING_OBJECT_TYPE getObjectType() {
    return objectType;
  }
  public void setObjectType(LINKING_OBJECT_TYPE objectType) {
    this.objectType = objectType;
  }
  public String getIdentifierType() {
    return identifierType;
  }
  public void setIdentifierType(String identifierType) {
    this.identifierType = identifierType;
  }
  public String getIdentifierValue() {
    return identifierValue;
  }
  public void setIdentifierValue(String identifierValue) {
    this.identifierValue = identifierValue;
  }
}
