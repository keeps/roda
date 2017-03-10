/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.Optional;

public class LiteOptionalWithCause implements Serializable {
  private static final long serialVersionUID = 2354151020405129417L;

  private SerializableOptional<LiteRODAObject> lite;
  private String exceptionClass;
  private String exceptionMessage;

  private LiteOptionalWithCause(Optional<LiteRODAObject> lite, String exceptionClass, String exceptionMessage) {
    super();
    this.lite = SerializableOptional.setOptional(lite);
    this.exceptionClass = exceptionClass;
    this.exceptionMessage = exceptionMessage;
  }

  public Optional<LiteRODAObject> getLite() {
    return lite.getOptional();
  }

  public String getExceptionClass() {
    return exceptionClass;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setLite(Optional<LiteRODAObject> lite) {
    this.lite = SerializableOptional.setOptional(lite);
  }

  public void setExpressionClass(String exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }

  public static LiteOptionalWithCause empty(Exception cause) {
    return new LiteOptionalWithCause(Optional.empty(), cause.getClass().getName(), cause.getMessage());
  }

  public static LiteOptionalWithCause empty(String exceptionClass, String exceptionMessage) {
    return new LiteOptionalWithCause(Optional.empty(), exceptionClass, exceptionMessage);
  }

  public static LiteOptionalWithCause of(LiteRODAObject value) {
    return new LiteOptionalWithCause(Optional.ofNullable(value), null, null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((exceptionClass == null) ? 0 : exceptionClass.hashCode());
    result = prime * result + ((exceptionMessage == null) ? 0 : exceptionMessage.hashCode());
    result = prime * result + lite.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LiteOptionalWithCause other = (LiteOptionalWithCause) obj;
    if (exceptionClass == null) {
      if (other.exceptionClass != null)
        return false;
    } else if (!exceptionClass.equals(other.exceptionClass))
      return false;
    if (exceptionMessage == null) {
      if (other.exceptionMessage != null)
        return false;
    } else if (!exceptionMessage.equals(other.exceptionMessage))
      return false;
    if (lite.getOptional().isPresent()) {
      if (!lite.getOptional().isPresent())
        return false;
    } else if (!lite.equals(other.lite))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "LiteRODAObjectWithCause [lite=" + lite + ", exceptionClass=" + exceptionClass + ", exceptionMessage="
      + exceptionMessage + "]";
  }

}
