/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.api.v1.utils;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class ApiException extends Exception {
  private static final long serialVersionUID = 4667937307148805083L;
  private int code;

  public ApiException(int code, String msg) {
    super(msg);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
