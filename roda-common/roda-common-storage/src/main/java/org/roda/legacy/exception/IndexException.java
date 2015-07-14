package org.roda.legacy.exception;

public class IndexException extends Exception {
	private static final long serialVersionUID = 8201040708309967966L;

	private int code;
	private String message;

	public IndexException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
