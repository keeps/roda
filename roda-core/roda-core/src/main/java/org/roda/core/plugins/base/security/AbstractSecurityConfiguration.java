/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
