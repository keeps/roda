/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;

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
