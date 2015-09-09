/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 *
 */
public interface SearchMessages extends Messages {

  // Common Search Messages
  @DefaultMessage("{0} results")
  public String totalResultsMessage(int total);

}
