/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is a parameter of a {@link Filter}.
 * 
 * @author Rui Castro
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@Type(value = BasicSearchFilterParameter.class, name = "BasicSearchFilterParameter"),
  @Type(value = EmptyKeyFilterParameter.class, name = "EmptyKeyFilterParameter"),
  @Type(value = LikeFilterParameter.class, name = "LikeFilterParameter"),
  @Type(value = NotSimpleFilterParameter.class, name = "NotSimpleFilterParameter"),
  @Type(value = OneOfManyFilterParameter.class, name = "OneOfManyFilterParameter"),
  @Type(value = DateIntervalFilterParameter.class, name = "DateIntervalFilterParameter"),
  @Type(value = DateRangeFilterParameter.class, name = "DateRangeFilterParameter"),
  @Type(value = LongRangeFilterParameter.class, name = "LongRangeFilterParameter"),
  @Type(value = StringRangeFilterParameter.class, name = "StringRangeFilterParameter"),
  @Type(value = RegexFilterParameter.class, name = "RegexFilterParameter"),
  @Type(value = SimpleFilterParameter.class, name = "SimpleFilterParameter"),
  @Type(value = OrFiltersParameters.class, name = "OrFiltersParameters")})
public abstract class FilterParameter implements Serializable {
  private static final long serialVersionUID = 3744111668897879761L;

  private String name;

  /**
   * Constructs an empty {@link FilterParameter}.
   */
  public FilterParameter() {
    // do nothing
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FilterParameter)) {
      return false;
    }
    FilterParameter other = (FilterParameter) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

}
