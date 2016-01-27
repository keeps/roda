/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Date;

public class IndexedObject implements Serializable {
  private static final long serialVersionUID = 1551695516173866430L;
  private String aipID;
  private String representationID;
  private String fileID;
  private String role;
  private String title;
  private String type;
  private String identifierType;
  private String identifierValue;
  public String getAipID() {
    return aipID;
  }
  public void setAipID(String aipID) {
    this.aipID = aipID;
  }
  public String getRepresentationID() {
    return representationID;
  }
  public void setRepresentationID(String representationID) {
    this.representationID = representationID;
  }
  public String getFileID() {
    return fileID;
  }
  public void setFileID(String fileID) {
    this.fileID = fileID;
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
