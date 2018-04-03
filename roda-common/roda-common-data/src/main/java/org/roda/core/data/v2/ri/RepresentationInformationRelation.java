/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import java.io.Serializable;

public class RepresentationInformationRelation implements Serializable {
  private static final long serialVersionUID = -903456414232867144L;

  private String relationType;
  private RelationObjectType objectType;
  private String link;
  private String title;

  public RepresentationInformationRelation() {
    this.setRelationType(null);
    this.setObjectType(null);
    this.setLink(null);
    this.setTitle(null);
  }

  public RepresentationInformationRelation(String relationType, RelationObjectType objectType, String link,
    String label) {
    this.setRelationType(relationType);
    this.setObjectType(objectType);
    this.setLink(link);
    this.setTitle(label);
  }

  public String getRelationType() {
    return relationType;
  }

  public void setRelationType(String relationType) {
    this.relationType = relationType;
  }

  public RelationObjectType getObjectType() {
    return objectType;
  }

  public void setObjectType(RelationObjectType objectType) {
    this.objectType = objectType;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String label) {
    this.title = label;
  }

  @Override
  public String toString() {
    return "RepresentationInformationRelation [relationType=" + relationType + ", objectType=" + objectType + ", link="
      + link + ", label=" + title + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((link == null) ? 0 : link.hashCode());
    result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
    result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
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
    RepresentationInformationRelation other = (RepresentationInformationRelation) obj;
    if (link == null) {
      if (other.link != null)
        return false;
    } else if (!link.equals(other.link))
      return false;
    if (objectType != other.objectType)
      return false;
    if (relationType == null) {
      if (other.relationType != null)
        return false;
    } else if (!relationType.equals(other.relationType))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    return true;
  }

}
