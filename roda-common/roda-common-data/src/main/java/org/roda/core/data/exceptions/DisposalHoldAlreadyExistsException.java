/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalHoldAlreadyExistsException extends AlreadyExistsException {


    private static final long serialVersionUID = -9204333449702855127L;

    /**
     * Constructs a new {@link DisposalHoldAlreadyExistsException}.
     */
    public DisposalHoldAlreadyExistsException() {
        // do nothing
    }

    /**
     * Construct a new {@link DisposalHoldAlreadyExistsException} with the error message.
     *
     * @param message
     *          the error message.
     */
    public DisposalHoldAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DisposalHoldAlreadyExistsException} with the given cause
     * exception.
     *
     * @param cause
     *          the cause exception.
     */
    public DisposalHoldAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DisposalHoldAlreadyExistsException} with the given error
     * message and cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public DisposalHoldAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
