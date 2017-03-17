/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.facet;

import java.util.List;

public class SimpleFacetParameter extends FacetParameter {

  private static final long serialVersionUID = -5377147008170114648L;

  public static final int DEFAULT_LIMIT = 100;

  private int limit = DEFAULT_LIMIT;

  public SimpleFacetParameter() {
    super();
  }

  public SimpleFacetParameter(String name) {
    super(name);
  }

  public SimpleFacetParameter(String name, int limit) {
    super(name);
    this.limit = limit;
  }

  public SimpleFacetParameter(String name, int limit, SORT sort) {
    super(name);
    this.limit = limit;
    this.setSort(sort);
  }

  public SimpleFacetParameter(String name, List<String> values) {
    super(name, values);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount) {
    super(name, values, minCount);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount, int limit) {
    super(name, values, minCount);
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Override
  public String toString() {
    return "SimpleFacetParameter [ super=" + super.toString() + ", limit=" + this.limit + "]";
  }
}
