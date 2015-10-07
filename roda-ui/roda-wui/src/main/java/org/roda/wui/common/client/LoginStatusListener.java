/**
 * 
 */
package org.roda.wui.common.client;

import org.roda.core.data.v2.RodaUser;

/**
 * @author Luis Faria
 * 
 */
public interface LoginStatusListener {

  /**
   * Called when the status of the login changes
   * 
   * @param user
   */
  public void onLoginStatusChanged(RodaUser user);

}
