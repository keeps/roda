package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public enum DisposalHoldAction implements Actionable.Action<DisposalHold> {
  NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_HOLD),
  EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_HOLD),
  LIFT(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD),
  DISASSOCIATE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);


  private final List<String> methods;

  DisposalHoldAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
