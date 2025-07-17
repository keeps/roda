package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class ResourceVersion implements Serializable {

  @Serial
  private static final long serialVersionUID = 2525596853384201705L;

  private String id;
  private Date createdDate;

  public ResourceVersion() {
    // empty constructor
  }

  public ResourceVersion(String id, Date createdDate) {
    this.id = id;
    this.createdDate = createdDate;
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
}
