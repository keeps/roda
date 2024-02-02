/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import jakarta.xml.bind.annotation.XmlTransient;

@jakarta.xml.bind.annotation.XmlRootElement
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class ApiResponseMessage {
  public static final int ERROR = 1;
  public static final int WARNING = 2;
  public static final int INFO = 3;
  public static final int OK = 4;
  public static final int TOO_BUSY = 5;

  private int code;
  private String type;
  private String message;

  public ApiResponseMessage() {
    // do nothing
  }

  public ApiResponseMessage(int code, String message) {
    this.code = code;
    switch (code) {
      case ERROR:
        setType("error");
        break;
      case WARNING:
        setType("warning");
        break;
      case INFO:
        setType("info");
        break;
      case OK:
        setType("ok");
        break;
      case TOO_BUSY:
        setType("too busy");
        break;
      default:
        setType("unknown");
        break;
    }
    this.message = message;
  }

  @XmlTransient
  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
