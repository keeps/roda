/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import java.util.Map;

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
	public void getAuthenticatedUser(AsyncCallback<AuthenticatedUser> callback);

	/**
	 * Login into RODA Core
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws RODAException
	 */
	public void login(String username, String password,
			AsyncCallback<AuthenticatedUser> callback);


	/**
	 * Get RODA properties
	 * 
	 * @return
	 */
	public void getRodaProperties(AsyncCallback<Map<String, String>> callback);

	void getRodaCasURL(AsyncCallback<String> callback);

	public void logout(AsyncCallback<AuthenticatedUser> asyncCallback);

}
