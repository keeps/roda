/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;

public class Relationship implements Serializable {
  private static final long serialVersionUID = 3561364925469496310L;

  private String relationType = null;
  private String relationName = null;
  private String relationObject = null;

  public Relationship() {
    super();
  }

  public Relationship(String relationType, String relationName, String relationObject) {
    this.relationType = relationType;
    this.relationName = relationName;
    this.relationObject = relationObject;
  }

  public String getRelationType() {
    return relationType;
  }

  public void setRelationType(String relationType) {
    this.relationType = relationType;
  }

  public String getRelationName() {
    return relationName;
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  public String getRelationObject() {
    return relationObject;
  }

  public void setRelationObject(String relationObject) {
    this.relationObject = relationObject;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((relationName == null) ? 0 : relationName.hashCode());
    result = prime * result + ((relationObject == null) ? 0 : relationObject.hashCode());
    result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
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
    Relationship other = (Relationship) obj;
    if (relationName == null) {
      if (other.relationName != null)
        return false;
    } else if (!relationName.equals(other.relationName))
      return false;
    if (relationObject == null) {
      if (other.relationObject != null)
        return false;
    } else if (!relationObject.equals(other.relationObject))
      return false;
    if (relationType == null) {
      if (other.relationType != null)
        return false;
    } else if (!relationType.equals(other.relationType))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Relationship [relationType=" + relationType + ", relationName=" + relationName + ", relationObject="
      + relationObject + "]";
  }

}
