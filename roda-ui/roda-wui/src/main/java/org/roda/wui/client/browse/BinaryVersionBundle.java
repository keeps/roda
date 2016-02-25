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

public class BinaryVersionBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private String id;
  private String message;
  private Date createdDate;

  public BinaryVersionBundle() {
    super();
  }

  public BinaryVersionBundle(String id, String message, Date createdDate) {
    super();
    this.id = id;
    this.message = message;
    this.createdDate = createdDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

}
