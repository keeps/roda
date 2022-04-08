package org.roda.core.data.exceptions;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class JobStateNotPendingException extends RODAException {


    private static final long serialVersionUID = -829610996201922888L;

    public JobStateNotPendingException() {
        super();
    }

    public JobStateNotPendingException(String message) {
        super(message);
    }

    public JobStateNotPendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobStateNotPendingException(Throwable cause) {
        super(cause);
    }

}
