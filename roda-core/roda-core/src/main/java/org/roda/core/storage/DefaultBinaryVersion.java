package org.roda.core.storage;

import java.util.Date;

public class DefaultBinaryVersion implements BinaryVersion {

  private static final long serialVersionUID = -606557401765095316L;

  private Binary binary;
  private String label;
  private Date createdDate;

  public DefaultBinaryVersion() {
    super();
  }

  public DefaultBinaryVersion(Binary binary, String label, Date createdDate) {
    super();
    this.binary = binary;
    this.label = label;
    this.createdDate = createdDate;
  }

  @Override
  public Binary getBinary() {
    return binary;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setBinary(Binary binary) {
    this.binary = binary;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  @Override
  public String toString() {
    return "DefaultBinaryVersion [binary=" + binary + ", label=" + label + ", createdDate=" + createdDate + "]";
  }

}
