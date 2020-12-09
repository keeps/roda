package org.roda.core.data.exceptions;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalRuleAlreadyExistsException extends AlreadyExistsException {
    private static final long serialVersionUID = -4589782633937474385L;

    /**
     * Constructs a new {@link DisposalRuleAlreadyExistsException}.
     */
    public DisposalRuleAlreadyExistsException() {
        // do nothing
    }

    /**
     * Construct a new {@link DisposalRuleAlreadyExistsException} with the error message.
     *
     * @param message
     *          the error message.
     */
    public DisposalRuleAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DisposalRuleAlreadyExistsException} with the given cause
     * exception.
     *
     * @param cause
     *          the cause exception.
     */
    public DisposalRuleAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DisposalRuleAlreadyExistsException} with the given error
     * message and cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public DisposalRuleAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
