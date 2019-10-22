/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.facet;

import java.io.Serializable;



import org.roda.core.data.common.RodaConstants;

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_FACET_VALUE)
public class FacetValue implements Serializable {
  private static final long serialVersionUID = 8898599554012120196L;

  private String label;
  private String value;
  private long count;

  public FacetValue() {
    super();
  }

  public FacetValue(String label, String value, long count) {
    super();
    this.label = label;
    this.value = value;
    this.count = count;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "FacetValue [value=" + value + ", count=" + count + "]";
  }

}