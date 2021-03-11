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
public class DisposalScheduleAlreadyExistsException extends AlreadyExistsException {


    private static final long serialVersionUID = -4589782633937474385L;

    /**
     * Constructs a new {@link DisposalScheduleAlreadyExistsException}.
     */
    public DisposalScheduleAlreadyExistsException() {
        // do nothing
    }

    /**
     * Construct a new {@link DisposalScheduleAlreadyExistsException} with the error message.
     *
     * @param message
     *          the error message.
     */
    public DisposalScheduleAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DisposalScheduleAlreadyExistsException} with the given cause
     * exception.
     *
     * @param cause
     *          the cause exception.
     */
    public DisposalScheduleAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DisposalScheduleAlreadyExistsException} with the given error
     * message and cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public DisposalScheduleAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
