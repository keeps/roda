/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.List;

public class AndFiltersParameters extends FiltersParameters {
  private static final long serialVersionUID = 6957719862558403942L;

  /**
   * Constructs an empty {@link AndFiltersParameters}.
   */
  public AndFiltersParameters() {
    super();
  }

  /**
   * Constructs a {@link AndFiltersParameters} cloning an existing
   * {@link AndFiltersParameters}.
   * 
   * @param orFiltersParameters
   *          the {@link AndFiltersParameters} to clone.
   */
  public AndFiltersParameters(AndFiltersParameters orFiltersParameters) {
    super(orFiltersParameters.getName(), orFiltersParameters.getValues());
  }

  /**
   * Constructs a {@link AndFiltersParameters} from a list of values.
   * 
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public AndFiltersParameters(String name, List<FilterParameter> values) {
    super(name, values);
  }

  @Override
  public String toString() {
    return "AndFiltersParameters [toString()=" + super.toString() + "]";
  }

}
