package org.roda.core.transaction;

import java.io.Serial;

import org.roda.core.data.exceptions.RODAException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RODATransactionException extends RODAException {
    @Serial
    private static final long serialVersionUID = -4005067334940107961L;

    public RODATransactionException() {
      super();
    }

    public RODATransactionException(String message) {
        super(message);
    }

    public RODATransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RODATransactionException(Throwable cause) {
        super(cause);
    }
}
