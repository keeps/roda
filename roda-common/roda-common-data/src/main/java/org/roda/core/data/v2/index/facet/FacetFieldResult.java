/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "facet_field_result")
public class FacetFieldResult implements Serializable {

  private static final long serialVersionUID = 4007898233996477150L;

  private String field;
  private long totalCount;
  private List<FacetValue> values;
  private List<String> selectedValues;

  public FacetFieldResult() {
    super();
  }

  public FacetFieldResult(String field, long totalCount) {
    super();
    this.field = field;
    this.totalCount = totalCount;
    this.values = new ArrayList<FacetValue>();
    this.selectedValues = new ArrayList<String>();
  }

  public FacetFieldResult(String field, long totalCount, List<String> selectedValues) {
    super();
    this.field = field;
    this.totalCount = totalCount;
    this.values = new ArrayList<FacetValue>();
    this.selectedValues = selectedValues;
  }

  public FacetFieldResult(String field, long totalCount, List<FacetValue> values, List<String> selectedValues) {
    super();
    this.field = field;
    this.totalCount = totalCount;
    this.values = values;
    this.selectedValues = selectedValues;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(long totalCount) {
    this.totalCount = totalCount;
  }

  public List<FacetValue> getValues() {
    return values;
  }

  public void setValues(List<FacetValue> values) {
    this.values = values;
  }

  public void addFacetValue(String label, String value, long count) {
    values.add(new FacetValue(label, value, count));
  }

  public List<String> getSelectedValues() {
    return selectedValues;
  }

  public void setSelectedValues(List<String> selectedValues) {
    this.selectedValues = selectedValues;
  }

  @Override
  public String toString() {
    return "FacetFieldResult [field=" + field + ", totalCount=" + totalCount + ", values=" + values + "]";
  }

}
