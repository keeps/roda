/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
