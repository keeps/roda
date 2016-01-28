/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Representation implements Serializable {

  private static final long serialVersionUID = 3658011895150894795L;

  @JsonIgnore
  private String aipId;

  private String id;
  private boolean original;

  public Representation() {
    super();
  }

  public Representation(String id, String aipId, boolean original) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.original = original;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public boolean isOriginal() {
    return original;
  }

  public void setOriginal(boolean original) {
    this.original = original;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (original ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Representation other = (Representation) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (original != other.original)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Representation [aipId=" + aipId + ", id=" + id + ", original=" + original + "]";
  }

}
