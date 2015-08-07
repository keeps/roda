package org.roda.model;

import org.roda.common.ServiceException;

public class ModelServiceException extends ServiceException {

	private static final long serialVersionUID = -3970536792438366410L;

	public ModelServiceException(String message, int code, Throwable cause) {
		super(message, code, cause);
	}

	public ModelServiceException(String message, int code) {
		super(message, code);
	}

}
