/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client;

import org.roda.core.data.v2.user.User;

/**
 * @author Luis Faria
 * 
 */
@FunctionalInterface
public interface LoginStatusListener {

  /**
   * Called when the status of the login changes
   * 
   * @param user
   */
  public void onLoginStatusChanged(User user);

}
