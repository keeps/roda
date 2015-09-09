package pt.gov.dgarq.roda.core.data.search;

import pt.gov.dgarq.roda.core.data.SearchParameter;

/**
 * This is the date range search parameter for the search service. It should be
 * used for fields that are intervals of normalised dates.
 * 
 * @author Rui Castro
 */
public class DateRangeSearchParameter extends SearchParameter {
  private static final long serialVersionUID = 3892549652511440392L;

  private String namePrefix = null;
  private String fromDate = null;
  private String toDate = null;

  /**
   * Constructs an empty {@link DateRangeSearchParameter}.
   */
  public DateRangeSearchParameter() {
  }

  /**
   * Constructs a new {@link DateRangeSearchParameter} cloning and existing
   * {@link DateRangeSearchParameter}.
   * 
   * @param dateRangeSearchParameter
   *          the {@link DateRangeSearchParameter} to clone.
   */
  public DateRangeSearchParameter(DateRangeSearchParameter dateRangeSearchParameter) {
    this(dateRangeSearchParameter.getNamePrefix(), dateRangeSearchParameter.getFromDate(), dateRangeSearchParameter
      .getToDate());
  }

  /**
   * Constructs a new {@link DateRangeSearchParameter} with the given
   * parameters.
   * 
   * @param namePrefix
   * @param fromDate
   * @param toDate
   */
  public DateRangeSearchParameter(String namePrefix, String fromDate, String toDate) {
    setNamePrefix(namePrefix);
    setFromDate(fromDate);
    setToDate(toDate);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof DateRangeSearchParameter) {
      DateRangeSearchParameter other = (DateRangeSearchParameter) obj;
      return this.getFromDate().equals(other.getFromDate()) && this.getToDate().equals(other.getToDate());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "DateRangeSearchParameter (" + namePrefix + ", " + fromDate + ", " + toDate + ")";
  }

  /**
   * Get Lucene sub query for this data range search
   * 
   * @return the sub query
   */
  @Override
  public String getSubQuery() {
    // [x,y] ∩ [a,b] é maior que conjunto vazio se:
    // x∊[a,b] || y∊[a,b] || (a∊[0,x] && b∊[y,∞])

    String fieldInitial = getNamePrefix() + ".initial";
    String fieldFinal = getNamePrefix() + ".final";

    String subQuery = "(";
    subQuery += fieldInitial + ":[" + getFromDate() + " TO " + getToDate() + "] OR ";
    subQuery += fieldFinal + ":[" + getFromDate() + " TO " + getToDate() + "]";
    subQuery += " OR (" + fieldInitial + ":[00000000 TO " + getFromDate() + "] AND " + fieldFinal + ":[" + getToDate()
      + " TO 99999999])";
    subQuery += ")";

    super.setSubQuery(subQuery);

    return subQuery;
  }

  /**
   * This method has no effect. To set the subquery use methods
   * {@link DateRangeSearchParameter#setNamePrefix(String)},
   * {@link DateRangeSearchParameter#setFromDate(String)} and
   * {@link DateRangeSearchParameter#setToDate(String)}.
   * 
   * @param subQuery
   */
  public void setSubQuery(String subQuery) {
  }

  /**
   * @return the fromDate
   */
  public String getFromDate() {
    return fromDate;
  }

  /**
   * @param fromDate
   *          the fromDate to set. Must be in format (YYYY-MM-DD or YYYYMMDD).
   */
  public void setFromDate(String fromDate) {
    if (fromDate == null) {
      this.fromDate = "00000000";
    } else {
      this.fromDate = fromDate.replaceAll("-", "");
    }
  }

  /**
   * @return the toDate
   */
  public String getToDate() {
    return toDate;
  }

  /**
   * @param toDate
   *          the toDate to set. Must be in format (YYYY-MM-DD or YYYYMMDD).
   */
  public void setToDate(String toDate) {
    if (toDate == null) {
      this.toDate = "99999999";
    } else {
      this.toDate = toDate.replaceAll("-", "");
    }
  }

  /**
   * @return the namePrefix
   */
  public String getNamePrefix() {
    return namePrefix;
  }

  /**
   * @param namePrefix
   *          the namePrefix to set
   */
  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
  }

}
