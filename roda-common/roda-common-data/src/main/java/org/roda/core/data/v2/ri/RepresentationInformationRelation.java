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

}
