package org.roda.core.data.v2.index.filter;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serial;
import java.util.Objects;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonTypeName("EDismaxSimpleQueryFilterParameter")
public class EDismaxSimpleQueryFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = 3614560283685151546L;

  private String value;

  /**
   * Constructs an empty {@link EDismaxSimpleQueryFilterParameter}.
   */
  public EDismaxSimpleQueryFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link EDismaxSimpleQueryFilterParameter} cloning an existing
   * {@link EDismaxSimpleQueryFilterParameter}.
   *
   * @param eDismaxSimpleQueryFilterParameter
   *          the {@link EDismaxSimpleQueryFilterParameter} to clone.
   */
  public EDismaxSimpleQueryFilterParameter(EDismaxSimpleQueryFilterParameter eDismaxSimpleQueryFilterParameter) {
    this(eDismaxSimpleQueryFilterParameter.getValue());
  }

  /**
   * Constructs a {@link EDismaxSimpleQueryFilterParameter} with the given
   * parameters.
   *
   * @param value
   */
  public EDismaxSimpleQueryFilterParameter(String value) {
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "EDismaxSimpleQueryFilterParameter(value=" + getValue() + ")";
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    EDismaxSimpleQueryFilterParameter that = (EDismaxSimpleQueryFilterParameter) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
