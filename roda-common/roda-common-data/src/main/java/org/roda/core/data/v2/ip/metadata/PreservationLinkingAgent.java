package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

public class PreservationLinkingAgent implements Serializable {
  private static final long serialVersionUID = 6569819087777313421L;
  private String role;
  private String title;
  private String type;
  private String identifierType;
  private String identifierValue;

  public PreservationLinkingAgent() {
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
