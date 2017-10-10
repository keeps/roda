package org.roda.core.data.v2.ri;

public enum RelationObjectType {
  AIP(4), REPRESENTATION_INFORMATION(3), WEB(2), TEXT(1);

  private int weight;

  private RelationObjectType() {
  }

  private RelationObjectType(int weight) {
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

}
