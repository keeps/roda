package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public enum DisposalScheduleAction implements Actionable.Action<DisposalSchedule> {
  NEW(RodaConstants.PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE),
  REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_SCHEDULE),
  DISASSOCIATE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE),
  EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_SCHEDULE);

  private final List<String> methods;

  DisposalScheduleAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
