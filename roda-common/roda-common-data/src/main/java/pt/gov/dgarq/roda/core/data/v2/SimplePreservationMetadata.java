package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;
import java.util.Date;

public class SimplePreservationMetadata implements Serializable {

  private static final long serialVersionUID = -200956181770322596L;

  private String id;
  private String label;
  private String model;
  private Date lastModifiedDate;
  private Date createdDate;
  private String state;
  private String type;

  private String aipId;
  private String representationId;
  private String fileId;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
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

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

}
