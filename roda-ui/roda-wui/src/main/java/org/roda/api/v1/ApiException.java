package org.roda.api.v1;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class ApiException extends Exception {
	private static final long serialVersionUID = 4667937307148805083L;
	private int code;

	public ApiException(int code, String msg) {
		super(msg);
		this.code = code;
	}
}
