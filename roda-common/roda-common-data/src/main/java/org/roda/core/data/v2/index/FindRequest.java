/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

/**
 * A request to a find operation.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FindRequest extends CountRequest {

  private static final long serialVersionUID = 5997470558754294987L;

  /** Sorter. */
  public Sorter sorter;
  /** Sublist (paging). */
  public Sublist sublist;
  /** Facets to return. */
  public Facets facets;
  /** Return only active resources? */
  public boolean onlyActive;
  /** For CSV results, export only facets? */
  public boolean exportFacets;
  /** The filename for exported CSV. */
  public String filename;

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
    this(classToReturn, filter, sorter, sublist, facets, onlyActive, false, "export.csv");
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
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param filename
   *          the filename for exported CSV.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets, final boolean onlyActive, final boolean exportFacets, final String filename) {
    super(classToReturn, filter);
    this.sorter = sorter;
    this.sublist = sublist;
    this.facets = facets;
    this.onlyActive = onlyActive;
    this.exportFacets = exportFacets;
    this.filename = filename;
  }

}
