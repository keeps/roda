/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.adapter.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class FacetParameter implements Serializable {
  private static final long serialVersionUID = 4927529408810091855L;
  public static final int DEFAULT_MIN_COUNT = 1;

  private String name;
  private List<String> values;
  private int minCount = DEFAULT_MIN_COUNT;

  public FacetParameter() {

  }

  public FacetParameter(String name) {
    super();
    this.name = name;
    this.values = new ArrayList<String>();
  }

  public FacetParameter(String name, List<String> values) {
    super();
    this.name = name;
    this.values = values;
  }

  public FacetParameter(String name, List<String> values, int minCount) {
    super();
    this.name = name;
    this.values = values;
    this.minCount = minCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public int getMinCount() {
    return minCount;
  }

  public void setMinCount(int minCount) {
    this.minCount = minCount;
  }

}
