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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@Type(value = SimpleFacetParameter.class, name = "SimpleFacetParameter"),
  @Type(value = RangeFacetParameter.class, name = "RangeFacetParameter")})
public abstract class FacetParameter implements Serializable {
  private static final long serialVersionUID = 4927529408810091855L;

  public static final int DEFAULT_MIN_COUNT = 1;

  private String name;
  private List<String> values;
  private int minCount = DEFAULT_MIN_COUNT;

  public FacetParameter() {
    this(null);
  }

  public FacetParameter(final String name) {
    this(name, new ArrayList<String>());
  }

  public FacetParameter(final String name, final List<String> values) {
    this(name, values, DEFAULT_MIN_COUNT);
  }

  public FacetParameter(final String name, final List<String> values, final int minCount) {
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

  public String toString() {
    return "FacetParameter [name=" + name + ", values=" + this.values + "]";
  }
}
