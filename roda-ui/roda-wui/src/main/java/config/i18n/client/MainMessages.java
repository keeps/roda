/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 *
 */
public interface MainMessages extends Messages {

  // Login panel
  @DefaultMessage("The login failed.\n Reason: {0}")
  public String loginFailure(String error);

  @DefaultMessage("Welcome {0}")
  public String loginSuccessMessage(String username);

  // Content Panel
  @DefaultMessage("Page not found: {0}")
  public String pageNotFound(String error);

  @DefaultMessage("RODA - {0}")
  public String windowTitle(String history);

}
