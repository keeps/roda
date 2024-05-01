package org.roda.core.data.v2.index.collapse;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MinMax implements Serializable {

  @Serial
  private static final long serialVersionUID = 5626390766634752703L;

  private String numericField;
  private boolean max = false;

  public static final MinMax NONE = new MinMax();

  public MinMax() {
    // do nothing
  }

  public MinMax(String numericField, boolean max) {
    this.numericField = numericField;
    this.max = max;
  }

  public String getNumericField() {
    return numericField;
  }

  public void setNumericField(String numericField) {
    this.numericField = numericField;
  }

  public boolean isMax() {
    return max;
  }

  public void setMax(boolean max) {
    this.max = max;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MinMax minMax = (MinMax) o;
    return max == minMax.max && Objects.equals(numericField, minMax.numericField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numericField, max);
  }

  @Override
  public String toString() {
    return "MinMax(" + "numericField=" + getNumericField() + ", max=" + isMax() + ')';
  }
}
