package org.roda.core.plugins.base.security;

import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.SecurityManager;

import jakarta.annotation.PostConstruct;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public abstract class AbstractSecurityConfiguration {
  private SecurityManager.SecurityService securityService;

  public void setSecurityService(SecurityManager.SecurityService securityService) {
    this.securityService = securityService;
  }

  @PostConstruct
  private void register() {
    RodaCoreFactory.getPluginManager().registerAuthPlugin(this);
  }

  public SecurityManager.SecurityService getSecurityService() {
    return securityService;
  }
}
