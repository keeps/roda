/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.adapter;

import java.io.Serializable;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;

/**
 * {@link ContentAdapter} is a {@link Filter}, a {@link Sorter} and a
 * {@link Sublist}. It's used in methods that return lists to adapt the results
 * to parameters specified in the {@link ContentAdapter}.
 * 
 * @author Rui Castro
 * 
 * @deprecated 20160125 hsilva: there is no need to use contentadapter anymore,
 *             so any code that still uses it should be changed
 * 
 */
public class ContentAdapter implements Serializable {
  private static final long serialVersionUID = -8788804279413264171L;

  private Filter filter = null;
  private Sorter sorter = null;
  private Sublist sublist = null;

  /**
   * Constructs an empty {@link ContentAdapter}.
   */
  public ContentAdapter() {
  }

  /**
   * Constructs a {@link ContentAdapter} cloning an existing
   * {@link ContentAdapter}.
   * 
   * @param contentAdaptor
   *          the {@link ContentAdapter} to clone.
   */
  public ContentAdapter(ContentAdapter contentAdaptor) {
    this(contentAdaptor.getFilter(), contentAdaptor.getSorter(), contentAdaptor.getSublist());
  }

  /**
   * Constructs a new {@link ContentAdapter} with the given parameters.
   * 
   * @param filter
   *          the filter.
   * @param sorter
   *          the sorter.
   * @param sublist
   *          the sublist.
   */
  public ContentAdapter(Filter filter, Sorter sorter, Sublist sublist) {
    setFilter(filter);
    setSorter(sorter);
    setSublist(sublist);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof ContentAdapter) {
      ContentAdapter other = (ContentAdapter) obj;
      equal = equal && (getFilter() == other.getFilter() || getFilter().equals(other.getFilter()));
      equal = equal && (getSorter() == other.getSorter() || getSorter().equals(other.getSorter()));
      equal = equal && (getSublist() == other.getSublist() || getSublist().equals(other.getSublist()));
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "ContentAdapter(filter=" + getFilter() + ", sorter=" + getSorter() + ", sublist=" + getSublist() + ")";
  }

  /**
   * @return the filter
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * @param filter
   *          the filter to set
   */
  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  /**
   * @return the sorter
   */
  public Sorter getSorter() {
    return sorter;
  }

  /**
   * @param sorter
   *          the sorter to set
   */
  public void setSorter(Sorter sorter) {
    this.sorter = sorter;
  }

  /**
   * @return the sublist
   */
  public Sublist getSublist() {
    return sublist;
  }

  /**
   * @param sublist
   *          the sublist to set
   */
  public void setSublist(Sublist sublist) {
    this.sublist = sublist;
  }

}
