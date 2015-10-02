package pt.gov.dgarq.roda.core.common;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class NotImplementedException extends RODAException {

  private static final long serialVersionUID = -6744205569453461540L;

  public NotImplementedException() {
    super();
  }

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(String message, NotImplementedException e) {
    super(message, e);
  }

}
