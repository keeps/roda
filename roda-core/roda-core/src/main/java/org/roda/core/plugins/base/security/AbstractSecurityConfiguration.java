package org.roda.core.plugins.base.security;

import org.roda.core.RodaCoreFactory;

import jakarta.annotation.PostConstruct;
import org.roda.core.plugins.SecurityManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class AbstractSecurityConfiguration {
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
