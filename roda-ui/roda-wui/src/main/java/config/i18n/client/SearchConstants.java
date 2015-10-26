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

import com.google.gwt.i18n.client.Constants;

/**
 * @author Luis Faria
 * 
 */
public interface SearchConstants extends Constants {

  // Basic Search
  @DefaultStringValue("Search in the repository:")
  public String basicSearchInputLabel();

  @DefaultStringValue("SEARCH")
  public String basicSearchButtonLabel();

  @DefaultStringValue("Please enter the words to search")
  public String basicSearchNoKeywords();

  // Advanced Search

  // Search Results
  @DefaultStringValue("searching...")
  public String searching();

  @DefaultStringValue("BROWSE")
  public String browseResult();

  @DefaultStringValue("loading...")
  public String searchResultLoading();

}
