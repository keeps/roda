package org.roda.core.data.v2.ip.disposalhold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.disposal.hold.DisposalHold;

public class UpdateDisposalHoldRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 3125581525913041977L;

  private DisposalHold disposalHold;
  private String details;

  public UpdateDisposalHoldRequest() {
    // empty constructor
  }

  public DisposalHold getDisposalHold() {
    return disposalHold;
  }

  public void setDisposalHold(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
