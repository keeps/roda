package org.roda.wui.common;

import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.RodaUser;

public class ControllerAssistant {
  private Date startDate;

  public ControllerAssistant() {
    this.startDate = new Date();
  }

  public void checkRoles(RodaUser user) throws AuthorizationDeniedException {
    UserUtility.checkRoles(user, this.getClass());
  }

  public void registerAction(RodaUser user, String aipId, LOG_ENTRY_STATE state, Object... parameters) {
    long duration = new Date().getTime() - startDate.getTime();
    RodaCoreService.registerAction(user, getInvokingClassName(), getInvokingMethodName(), aipId, duration, state,
      parameters);
  }

  public void registerAction(RodaUser user, LOG_ENTRY_STATE state, Object... parameters) {
    registerAction(user, (String) null, state, parameters);
  }

  public void registerAction(RodaUser user, LOG_ENTRY_STATE state) {
    registerAction(user, (String) null, state);
  }

  private String getInvokingMethodName() {
    return this.getClass().getEnclosingMethod().getName();
  }

  private String getInvokingClassName() {
    return this.getClass().getEnclosingMethod().getDeclaringClass().getName();
  }
}
