/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.Serializable;

public class ParseError implements Serializable {

  private static final long serialVersionUID = -7269527212240870004L;

  private String message;
  private int lineNumber;
  private int columnNumber;
  private String publicId;
  private String systemId;

  public ParseError() {
    super();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setColumnNumber(int columnNumber) {
    this.columnNumber = columnNumber;
  }

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + columnNumber;
    result = prime * result + lineNumber;
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((publicId == null) ? 0 : publicId.hashCode());
    result = prime * result + ((systemId == null) ? 0 : systemId.hashCode());
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
    ParseError other = (ParseError) obj;
    if (columnNumber != other.columnNumber)
      return false;
    if (lineNumber != other.lineNumber)
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (publicId == null) {
      if (other.publicId != null)
        return false;
    } else if (!publicId.equals(other.publicId))
      return false;
    if (systemId == null) {
      if (other.systemId != null)
        return false;
    } else if (!systemId.equals(other.systemId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ParseError [message=" + message + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber
      + ", publicId=" + publicId + ", systemId=" + systemId + "]";
  }

}
