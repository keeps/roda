package org.roda.storage;

public class StorageActionException extends Exception {

	private static final long serialVersionUID = 1L;

	public static final int BAD_REQUEST = 400;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int ALREADY_EXISTS = 409;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int NOT_IMPLEMENTED = 501;

	private int code;

	public StorageActionException(String message, int code) {
		super(message);
		this.code = code;
	}

	public StorageActionException(String message, int code, Throwable cause) {
		super(message, cause);
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
		return super.getMessage();
	}

}
