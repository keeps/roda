package org.roda.wui.common;

import java.util.Collections;
import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.user.RodaUser;

public class ControllerAssistant {
  private Date startDate;

  public ControllerAssistant() {
    this.startDate = new Date();
  }

  public void checkRoles(RodaUser user) throws AuthorizationDeniedException {
    UserUtility.checkRoles(user, this.getClass());
  }

  public void registerAction(RodaUser user, String aipId, Object... parameters) {
    long duration = new Date().getTime() - startDate.getTime();
    RodaCoreService.registerAction(user, getInvokingClassName(), getInvokingMethodName(), aipId, duration, parameters);
  }

  public void registerAction(RodaUser user, Object... parameters) {
    registerAction(user, null, parameters);
  }

  public void registerAction(RodaUser user) {
    registerAction(user, (String)null);
  }

  private String getInvokingMethodName() {
    return this.getClass().getEnclosingMethod().getName();
  }

  private String getInvokingClassName() {
    return this.getClass().getEnclosingMethod().getDeclaringClass().getName();
  }
}
