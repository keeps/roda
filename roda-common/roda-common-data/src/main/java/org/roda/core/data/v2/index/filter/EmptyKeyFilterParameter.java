/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 *
 */
@JsonTypeName("EmptyKeyFilterParameter")
public class EmptyKeyFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = 5888125949326684987L;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public EmptyKeyFilterParameter() {
    // do nothing
  }

  public EmptyKeyFilterParameter(EmptyKeyFilterParameter emptyKeyFilterParameter) {
    this(emptyKeyFilterParameter.getName());
  }

  public EmptyKeyFilterParameter(String name) {
    setName(name);
  }

  @Override
  public String toString() {
    return "EmptyKeyFilterParameter [getName()=" + getName() + ", getClass()=" + getClass() + ", hashCode()="
      + hashCode() + ", toString()=" + super.toString() + "]";
  }

}
