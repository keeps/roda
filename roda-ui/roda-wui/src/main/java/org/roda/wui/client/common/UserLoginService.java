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

import java.util.Map;

import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface UserLoginService extends RemoteService {

  /**
   * Service URI
   */
  public static final String SERVICE_URI = "userlogin";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static UserLoginServiceAsync getInstance() {

      UserLoginServiceAsync instance = (UserLoginServiceAsync) GWT.create(UserLoginService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Get the authenticated user
   * 
   * @return
   * @throws RODAException
   */
  public User getAuthenticatedUser() throws RODAException;

  /**
   * Login into RODA Core
   * 
   * @param username
   * @param password
   * @return
   * @throws RODAException
   */
  public User login(String username, String password) throws AuthenticationDeniedException, GenericException;

  /**
   * Get RODA properties
   * 
   * @return
   */
  public Map<String, String> getRodaProperties();

}
