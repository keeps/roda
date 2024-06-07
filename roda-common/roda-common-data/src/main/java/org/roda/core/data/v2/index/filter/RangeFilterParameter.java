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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(value = DateIntervalFilterParameter.class, name = "DateIntervalFilterParameter"),
  @JsonSubTypes.Type(value = DateRangeFilterParameter.class, name = "DateRangeFilterParameter"),
  @JsonSubTypes.Type(value = LongRangeFilterParameter.class, name = "LongRangeFilterParameter"),
  @JsonSubTypes.Type(value = StringRangeFilterParameter.class, name = "StringRangeFilterParameter")})
@Schema(type = "object", subTypes = {DateIntervalFilterParameter.class, DateRangeFilterParameter.class,
  LongRangeFilterParameter.class, StringRangeFilterParameter.class}, discriminatorMapping = {
    @DiscriminatorMapping(value = "DateIntervalFilterParameter", schema = DateIntervalFilterParameter.class),
    @DiscriminatorMapping(value = "DateRangeFilterParameter", schema = DateRangeFilterParameter.class),
    @DiscriminatorMapping(value = "LongRangeFilterParameter", schema = LongRangeFilterParameter.class),
    @DiscriminatorMapping(value = "StringRangeFilterParameter", schema = StringRangeFilterParameter.class)}, discriminatorProperty = "@type")
public abstract class RangeFilterParameter<T extends Serializable> extends FilterParameter {
  @Serial
  private static final long serialVersionUID = -2923383960685420739L;

  private T fromValue;
  private T toValue;

  /**
   * Constructs an empty {@link RangeFilterParameter}.
   */
  public RangeFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link RangeFilterParameter} cloning an existing
   * {@link RangeFilterParameter}.
   *
   * @param rangeFilterParameter
   *          the {@link RangeFilterParameter} to clone.
   */
  public RangeFilterParameter(RangeFilterParameter<T> rangeFilterParameter) {
    this(rangeFilterParameter.getName(), rangeFilterParameter.getFromValue(), rangeFilterParameter.getToValue());
  }

  /**
   * Constructs a {@link RangeFilterParameter} with the given parameters.
   *
   * @param name
   * @param fromValue
   * @param toValue
   */
  public RangeFilterParameter(String name, T fromValue, T toValue) {
    setName(name);
    setFromValue(fromValue);
    setToValue(toValue);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "RangeFilterParameter(name=" + getName() + ", fromValue=" + getFromValue() + ", toValue=" + getToValue()
      + ")";
  }

  /**
   * @return the fromValue
   */
  public T getFromValue() {
    return fromValue;
  }

  /**
   * @param fromValue
   *          the fromValue to set
   */
  public void setFromValue(T fromValue) {
    this.fromValue = fromValue;
  }

  /**
   * @return the toValue
   */
  public T getToValue() {
    return toValue;
  }

  /**
   * @param toValue
   *          the toValue to set
   */
  public void setToValue(T toValue) {
    this.toValue = toValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fromValue == null) ? 0 : fromValue.hashCode());
    result = prime * result + ((toValue == null) ? 0 : toValue.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RangeFilterParameter other = (RangeFilterParameter) obj;
    if (fromValue == null) {
      if (other.fromValue != null)
        return false;
    } else if (!fromValue.equals(other.fromValue))
      return false;
    if (toValue == null) {
      if (other.toValue != null)
        return false;
    } else if (!toValue.equals(other.toValue))
      return false;
    return true;
  }

}
