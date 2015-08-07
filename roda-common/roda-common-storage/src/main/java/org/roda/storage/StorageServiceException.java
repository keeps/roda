package org.roda.storage;

import org.roda.common.ServiceException;

public class StorageServiceException extends ServiceException {

	private static final long serialVersionUID = 473749530367806276L;

	public StorageServiceException(String message, int code, Throwable cause) {
		super(message, code, cause);
	}

	public StorageServiceException(String message, int code) {
		super(message, code);
	}

}
