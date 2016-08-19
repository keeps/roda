package org.roda.wui.api.v1.utils;

import java.io.Serializable;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;

/**
 * A request to a find operation.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FindRequest implements Serializable {

  private static final long serialVersionUID = 5997470558754294987L;

  /** Class name of resources to return. */
  public String classToReturn;
  /** Filter. */
  public Filter filter;
  /** Sorter. */
  public Sorter sorter;
  /** Sublist (paging). */
  public Sublist sublist;
  /** Facets to return. */
  public Facets facets;
  /** Return only active resources? */
  public boolean onlyActive;

  /**
   * Constructor.
   */
  public FindRequest() {
    this(null, new Filter(), new Sorter(), new Sublist(), new Facets(), true);
  }

  /**
   * Constructor.
   * 
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   * @param onlyActive
   *          Return only active resources?
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets, final boolean onlyActive) {
    this.classToReturn = classToReturn;
    this.filter = filter;
    this.sorter = sorter;
    this.sublist = sublist;
    this.facets = facets;
    this.onlyActive = onlyActive;
  }

}
