/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.facet;

public class RangeFacetParameter extends FacetParameter {

  private static final long serialVersionUID = 2190074263722637165L;

  // XXX these are strings because one might pass dates or numbers
  private String start;
  private String end;
  private String gap;

  public RangeFacetParameter() {
    super();
  }

  public RangeFacetParameter(String name) {
    super(name);
  }

  public RangeFacetParameter(String name, String start, String end, String gap) {
    super(name);
    this.start = start;
    this.end = end;
    this.gap = gap;

  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public String getGap() {
    return gap;
  }

  public void setGap(String gap) {
    this.gap = gap;
  }

}
