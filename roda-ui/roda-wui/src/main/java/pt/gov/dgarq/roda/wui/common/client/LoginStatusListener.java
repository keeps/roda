/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

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
	public void onLoginStatusChanged(AuthenticatedUser user);

}
