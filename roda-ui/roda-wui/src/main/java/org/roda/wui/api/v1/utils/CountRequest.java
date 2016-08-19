package org.roda.wui.api.v1.utils;

import java.io.Serializable;

import org.roda.core.data.adapter.filter.Filter;

/**
 * A request to a count operation.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CountRequest implements Serializable {

  private static final long serialVersionUID = -6793510712321710035L;

  /** Class name of resources to return. */
  public String classToReturn;
  /** Filter. */
  public Filter filter;

  /**
   * Constructor.
   */
  public CountRequest() {
    this(null, new Filter());
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   */
  public CountRequest(final String classToReturn, final Filter filter) {
    this.classToReturn = classToReturn;
    this.filter = filter;
  }

}
