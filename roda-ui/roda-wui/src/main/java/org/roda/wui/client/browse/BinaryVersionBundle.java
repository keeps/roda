/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class BinaryVersionBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private String id;
  private Date createdDate;
  private Map<String, String> properties;

  public BinaryVersionBundle() {
    super();
  }

  public BinaryVersionBundle(String id, Date createdDate, Map<String, String> properties) {
    super();
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
