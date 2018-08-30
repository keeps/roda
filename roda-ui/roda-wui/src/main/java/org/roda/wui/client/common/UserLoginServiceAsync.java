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
package org.roda.wui.client.common;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface UserLoginServiceAsync {

  /**
   * Get the authenticated user
   * 
   * @return
   * @throws RODAException
   */
  public void getAuthenticatedUser(AsyncCallback<User> callback);

  /**
   * Login into RODA Core
   * 
   * @param username
   * @param password
   * @return
   * @throws RODAException
   */
  public void login(String username, String password, AsyncCallback<User> callback);
}
