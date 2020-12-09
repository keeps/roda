package org.roda.core.data.exceptions;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalScheduleNotValidException extends GenericException{
    private static final long serialVersionUID = 809098516488134186L;


    /**
     * Constructs a new {@link DisposalScheduleNotValidException}.
     */
    public DisposalScheduleNotValidException() {
        // do nothing
    }

    /**
     * Construct a new {@link DisposalScheduleNotValidException} with the error message.
     *
     * @param message
     *          the error message.
     */
    public DisposalScheduleNotValidException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DisposalScheduleNotValidException} with the given cause
     * exception.
     *
     * @param cause
     *          the cause exception.
     */
    public DisposalScheduleNotValidException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DisposalScheduleNotValidException} with the given error
     * message and cause exception.
     *
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public DisposalScheduleNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

}
