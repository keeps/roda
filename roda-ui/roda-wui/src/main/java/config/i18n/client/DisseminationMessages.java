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
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 *
 */
public interface DisseminationMessages extends Messages {

  @DefaultMessage("{0} to {1}")
  public String dateRangeValue(String dateInitial, String dateFinal);

  // Descriptive Metadata
  // * Description Group Panel
  @DefaultMessage("{0}")
  public String descriptionLabel(String label);

}
