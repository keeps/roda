/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.index.filter.Filter;

/**
 * A request to a count operation.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CountRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -6793510712321710035L;

  /** Class name of resources to return. */
  private String classToReturn;
  /** Filter. */
  private Filter filter;
  /** Return only active resources? */
  private boolean onlyActive;

  /**
   * Constructor.
   */
  public CountRequest() {
    this(null, new Filter(), false);
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   */
  public CountRequest(final String classToReturn, final Filter filter, final boolean onlyActive) {
    this.classToReturn = classToReturn;
    this.filter = filter;
    this.onlyActive = onlyActive;
  }

  public String getClassToReturn() {
    return classToReturn;
  }

  public Filter getFilter() {
    return filter;
  }

  public boolean isOnlyActive() {
    return onlyActive;
  }
}
