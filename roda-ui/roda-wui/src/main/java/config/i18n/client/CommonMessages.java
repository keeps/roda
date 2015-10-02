/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author lfaria
 * 
 */
public interface CommonMessages extends Messages {

  // User Login
  @DefaultMessage("The user name and/or password that you used are not valid. \nException: {0}.")
  String loginFailed(String exception);

  @DefaultMessage("There was an error communicating with the RODA service: {0}.")
  String connectionFailed(String exception);

  @DefaultMessage("An error occurred in the RODA services: {0}.")
  String serviceFailed(String exception);

  @DefaultMessage("An error occurred in the RODA client: {0}")
  String rodaClientFailed(String message);

  @DefaultMessage("An error occurred: {0}.")
  String genericFailure(String exception);

  // Logger
  @DefaultMessage("{0}")
  String error(String object);

  // Lazy vertical list
  @DefaultMessage("The print list has {1} elements, which exceeds the maximum of {0} elements. You can add more filters to reduce the number of elements to print. Want to print the first {0} of the list?")
  String reportPrintMaxSize(int maxReportSize, int total);

  /***********************************************/
  /****************   NEW   **********************/
  /***********************************************/
  
  @DefaultMessage("{0}: {1}")
  String logParameter(String name, String value);

}
