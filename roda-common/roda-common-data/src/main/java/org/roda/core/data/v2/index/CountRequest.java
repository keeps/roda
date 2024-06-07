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

  /** Filter. */
  private Filter filter;
  /** Return only active resources? */
  private boolean onlyActive;

  /**
   * Constructor.
   */
  public CountRequest() {
    this(new Filter(), false);
  }

  /**
   * Constructor.
   *
   * @param filter
   *          Filter.
   */
  public CountRequest(final Filter filter, final boolean onlyActive) {
    this.filter = filter;
    this.onlyActive = onlyActive;
  }

  public Filter getFilter() {
    return filter;
  }

  public boolean isOnlyActive() {
    return onlyActive;
  }
}
