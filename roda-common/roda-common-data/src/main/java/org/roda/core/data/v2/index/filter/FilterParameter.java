/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This is a parameter of a {@link Filter}.
 *
 * @author Rui Castro
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(value = BasicSearchFilterParameter.class, name = "BasicSearchFilterParameter"),
  @JsonSubTypes.Type(value = EmptyKeyFilterParameter.class, name = "EmptyKeyFilterParameter"),
  @JsonSubTypes.Type(value = LikeFilterParameter.class, name = "LikeFilterParameter"),
  @JsonSubTypes.Type(value = NotSimpleFilterParameter.class, name = "NotSimpleFilterParameter"),
  @JsonSubTypes.Type(value = OneOfManyFilterParameter.class, name = "OneOfManyFilterParameter"),
  @JsonSubTypes.Type(value = DateIntervalFilterParameter.class, name = "DateIntervalFilterParameter"),
  @JsonSubTypes.Type(value = DateRangeFilterParameter.class, name = "DateRangeFilterParameter"),
  @JsonSubTypes.Type(value = LongRangeFilterParameter.class, name = "LongRangeFilterParameter"),
  @JsonSubTypes.Type(value = StringRangeFilterParameter.class, name = "StringRangeFilterParameter"),
  @JsonSubTypes.Type(value = SimpleFilterParameter.class, name = "SimpleFilterParameter"),
  @JsonSubTypes.Type(value = OrFiltersParameters.class, name = "OrFiltersParameters"),
  @JsonSubTypes.Type(value = AndFiltersParameters.class, name = "AndFiltersParameters"),
  @JsonSubTypes.Type(value = AllFilterParameter.class, name = "AllFilterParameter"),
  @JsonSubTypes.Type(value = BlockJoinParentFilterParameter.class, name = "BlockJoinParentFilterParameter")})
@Schema(type = "object", subTypes = {BasicSearchFilterParameter.class, EmptyKeyFilterParameter.class,
  LikeFilterParameter.class, NotSimpleFilterParameter.class, OneOfManyFilterParameter.class,
  DateIntervalFilterParameter.class, DateRangeFilterParameter.class, LongRangeFilterParameter.class,
  StringRangeFilterParameter.class, SimpleFilterParameter.class, OrFiltersParameters.class, AndFiltersParameters.class,
  AllFilterParameter.class, BlockJoinParentFilterParameter.class}, discriminatorMapping = {
    @DiscriminatorMapping(value = "BasicSearchFilterParameter", schema = BasicSearchFilterParameter.class),
    @DiscriminatorMapping(value = "LikeFilterParameter", schema = LikeFilterParameter.class),
    @DiscriminatorMapping(value = "NotSimpleFilterParameter", schema = NotSimpleFilterParameter.class),
    @DiscriminatorMapping(value = "OneOfManyFilterParameter", schema = OneOfManyFilterParameter.class),
    @DiscriminatorMapping(value = "DateIntervalFilterParameter", schema = DateIntervalFilterParameter.class),
    @DiscriminatorMapping(value = "DateRangeFilterParameter", schema = DateRangeFilterParameter.class),
    @DiscriminatorMapping(value = "LongRangeFilterParameter", schema = LongRangeFilterParameter.class),
    @DiscriminatorMapping(value = "StringRangeFilterParameter", schema = StringRangeFilterParameter.class),
    @DiscriminatorMapping(value = "SimpleFilterParameter", schema = SimpleFilterParameter.class),
    @DiscriminatorMapping(value = "OrFiltersParameters", schema = OrFiltersParameters.class),
    @DiscriminatorMapping(value = "AndFiltersParameters", schema = AndFiltersParameters.class),
    @DiscriminatorMapping(value = "AllFilterParameter", schema = AllFilterParameter.class),
    @DiscriminatorMapping(value = "BlockJoinParentFilterParameter", schema = BlockJoinParentFilterParameter.class),}, discriminatorProperty = "@type")
public abstract class FilterParameter implements Serializable {
  @Serial
  private static final long serialVersionUID = 3744111668897879761L;

  private String name;

  /**
   * Constructs an empty {@link FilterParameter}.
   */
  protected FilterParameter() {
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
