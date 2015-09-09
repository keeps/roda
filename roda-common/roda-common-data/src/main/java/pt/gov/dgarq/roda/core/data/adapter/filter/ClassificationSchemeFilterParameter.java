package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.util.Arrays;
import java.util.List;

public class ClassificationSchemeFilterParameter extends FilterParameter {

  private static final long serialVersionUID = 8108821828334254597L;
  private String classificationSchemeId = null;
  // FIXME change this to list
  private List<String> possibleClassesPids = null;

  public ClassificationSchemeFilterParameter() {
    setName("classificationscheme");
  }

  public ClassificationSchemeFilterParameter(String classificationSchemeId, List<String> possibleClassesPids) {
    setName("classificationscheme");
    this.classificationSchemeId = classificationSchemeId;
    this.possibleClassesPids = possibleClassesPids;
  }

  public String getClassificationSchemeId() {
    return classificationSchemeId;
  }

  public void setClassificationSchemeId(String classificationSchemeId) {
    this.classificationSchemeId = classificationSchemeId;
  }

  public List<String> getPossibleClassesPids() {
    return possibleClassesPids;
  }

  public void setPossibleClassesPids(List<String> possibleClassesPids) {
    this.possibleClassesPids = possibleClassesPids;
  }

  @Override
  public String toString() {
    return "ClassificationSchemeFilterParameter [classificationSchemeId=" + classificationSchemeId
      + ", possibleClassesPids=" + possibleClassesPids + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ClassificationSchemeFilterParameter other = (ClassificationSchemeFilterParameter) obj;
    if (classificationSchemeId == null) {
      if (other.classificationSchemeId != null) {
        return false;
      }
    } else if (!classificationSchemeId.equals(other.classificationSchemeId)) {
      return false;
    }
    if (!possibleClassesPids.equals(other.possibleClassesPids)) {
      return false;
    }
    return true;
  }

}
