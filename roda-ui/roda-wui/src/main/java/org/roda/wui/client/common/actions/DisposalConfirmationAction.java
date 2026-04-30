package org.roda.wui.client.common.actions;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public enum DisposalConfirmationAction implements Actionable.Action<DisposalConfirmation> {
  NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION),
  DESTROY(RodaConstants.PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION),
  PERM_DELETE(RodaConstants.PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION),
  RESTORE(RodaConstants.PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION);

  private final List<String> methods;

  DisposalConfirmationAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
