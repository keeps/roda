/**
 * 
 */
package org.roda.wui.common.client;

import java.util.Map;

import org.roda.core.common.RODAException;
import org.roda.core.data.v2.RodaUser;

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
  public void getAuthenticatedUser(AsyncCallback<RodaUser> callback);

  /**
   * Login into RODA Core
   * 
   * @param username
   * @param password
   * @return
   * @throws RODAException
   */
  public void login(String username, String password, AsyncCallback<RodaUser> callback);

  /**
   * Get RODA properties
   * 
   * @return
   */
  public void getRodaProperties(AsyncCallback<Map<String, String>> callback);

}
