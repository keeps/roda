package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ResourceVersion implements Serializable {

  @Serial
  private static final long serialVersionUID = 2525596853384201705L;

  private String id;
  private Date createdDate;
  private Map<String, String> properties;

  public ResourceVersion() {
    // empty constructor
  }

  public ResourceVersion(String id, Date createdDate, Map<String, String> properties) {
    this.id = id;
    this.createdDate = createdDate;
    this.properties = properties;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
