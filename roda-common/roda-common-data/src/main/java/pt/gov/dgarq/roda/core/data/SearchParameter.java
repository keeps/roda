package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;

/**
 * This class is the base class for all search parameters. It should be
 * sub-classed by classes that can be used as search parameters for the search
 * service.
 * <p>
 * <strong> Subclasses must override methods
 * {@link SearchParameter#getSubQuery()} and
 * {@link SearchParameter#setSubQuery(String)}.</strong>
 * </p>
 * 
 * @author Rui Castro
 */
public class SearchParameter implements Serializable {
  private static final long serialVersionUID = 6850221739847515140L;

  private String subQuery = null;

  /**
   * Constructs an empty {@link SearchParameter}.
   */
  public SearchParameter() {
  }

  /**
   * Constructs a {@link SearchParameter} cloning an existing
   * {@link SearchParameter}.
   * 
   * @param searchParameter
   *          the {@link SearchParameter} to clone.
   */
  public SearchParameter(SearchParameter searchParameter) {
    this(searchParameter.getSubQuery());
  }

  /**
   * Constructs a new SearchParameter with the given subquery.
   * 
   * @param subQuery
   */
  public SearchParameter(String subQuery) {
    setSubQuery(subQuery);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof SearchParameter) {
      SearchParameter other = (SearchParameter) obj;
      return this.getSubQuery().equals(other.getSubQuery());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SearchParameter (" + getSubQuery() + ")";
  }

  /**
   * Gets the sub query for this {@link SearchParameter}.
   * 
   * @return a {@link String} with the subquery for this {@link SearchParameter}
   *         .
   */
  public String getSubQuery() {
    return subQuery;
  }

  /**
   * @param subQuery
   *          the subQuery to set
   */
  public void setSubQuery(String subQuery) {
    this.subQuery = subQuery;
  }

}
