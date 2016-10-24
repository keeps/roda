/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DefaultBinaryVersion implements BinaryVersion {

  private static final long serialVersionUID = -606557401765095316L;

  @JsonIgnore
  private Binary binary;
  private String id;
  private Date createdDate;
  private Map<String, String> properties;

  public DefaultBinaryVersion() {
    super();
  }

  public DefaultBinaryVersion(Binary binary, String id, Date createdDate, Map<String, String> properties) {
    super();
    this.binary = binary;
    this.id = id;
    this.createdDate = createdDate;
    this.properties = properties;
  }

  @Override
  public Binary getBinary() {
    return binary;
  }

  public void setBinary(Binary binary) {
    this.binary = binary;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  @Override
  public String toString() {
    return "DefaultBinaryVersion [binary=" + binary + ", id=" + id + ", createdDate=" + createdDate + ", properties="
      + properties + "]";
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
