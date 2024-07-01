/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(value = OrFiltersParameters.class, name = "OrFiltersParameters"),
  @JsonSubTypes.Type(value = AndFiltersParameters.class, name = "AndFiltersParameters")})
@Schema(type = "object", subTypes = {OrFiltersParameters.class, AndFiltersParameters.class}, discriminatorMapping = {
  @DiscriminatorMapping(value = "OrFiltersParameters", schema = OrFiltersParameters.class),
  @DiscriminatorMapping(value = "AndFiltersParameters", schema = AndFiltersParameters.class)}, discriminatorProperty = "@type")
public abstract class FiltersParameters extends FilterParameter {
  @Serial
  private static final long serialVersionUID = -7444113772637341849L;

  private List<FilterParameter> values = null;

  /**
   * Constructs an empty {@link FiltersParameters}.
   */
  public FiltersParameters() {
    // do nothing
  }

  /**
   * Constructs a {@link FiltersParameters} cloning an existing
   * {@link FiltersParameters}.
   *
   * @param filtersParameters
   *          the {@link FiltersParameters} to clone.
   */
  public FiltersParameters(FiltersParameters filtersParameters) {
    this(filtersParameters.getValues());
  }

  /**
   * Constructs a {@link FiltersParameters} from a list of values.
   *
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public FiltersParameters(List<FilterParameter> values) {
    setName("FiltersParameters");
    setValues(values);
  }

  public List<FilterParameter> getValues() {
    return values;
  }

  public void setValues(List<FilterParameter> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "FiltersParameters [values=" + values + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof FiltersParameters))
      return false;
    FiltersParameters other = (FiltersParameters) obj;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;
    return true;
  }

}
