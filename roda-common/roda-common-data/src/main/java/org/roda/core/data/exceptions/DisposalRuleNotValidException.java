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
public class DisposalRuleNotValidException extends GenericException {

    private static final long serialVersionUID = 2413699496731974126L;


    /**
     * Constructs a new {@link DisposalRuleNotValidException}.
     */
    public DisposalRuleNotValidException() {
        // do nothing
    }

    /**
     * Construct a new {@link DisposalRuleNotValidException} with the error message.
     *
     * @param message
     *          the error message.
     */
    public DisposalRuleNotValidException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DisposalRuleNotValidException} with the given cause
     * exception.
     *
     * @param cause
     *          the cause exception.
     */
    public DisposalRuleNotValidException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DisposalRuleNotValidException} with the given error
     * message and cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public DisposalRuleNotValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
