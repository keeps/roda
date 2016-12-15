/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.List;

public class OrFiltersParameters extends FiltersParameters {
  private static final long serialVersionUID = -7444113772637341849L;

  /**
   * Constructs an empty {@link OrFiltersParameters}.
   */
  public OrFiltersParameters() {
    super();
  }

  /**
   * Constructs a {@link OrFiltersParameters} cloning an existing
   * {@link OrFiltersParameters}.
   * 
   * @param orFiltersParameters
   *          the {@link OrFiltersParameters} to clone.
   */
  public OrFiltersParameters(OrFiltersParameters orFiltersParameters) {
    super(orFiltersParameters.getValues());
  }

  /**
   * Constructs a {@link OrFiltersParameters} from a list of values.
   * 
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public OrFiltersParameters(List<FilterParameter> values) {
    super(values);
  }

  @Override
  public String toString() {
    return "OrFiltersParameters [toString()=" + super.toString() + "]";
  }

}
