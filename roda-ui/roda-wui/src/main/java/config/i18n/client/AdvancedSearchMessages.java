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
public interface AdvancedSearchMessages extends Messages {

  @DefaultMessage("All words: {0}; in fields: {1}")
  public String allWordsIn(String keywords, String fields);

  @DefaultMessage("All words: {0}; in all fields")
  public String allWordsInAllFields(String keywords);

  @DefaultMessage("At least one of the words: {0}; in fields: {1}")
  public String atLeastOneWordIn(String keywords, String fields);

  @DefaultMessage("At least one of the words: {0}; in all fields")
  public String atLeastOneWordInAllFields(String keywords);

  @DefaultMessage("The exact phrase ''{0}'' in fields: {1}")
  public String exactPhraseIn(String keywords, String fields);

  @DefaultMessage("The exact phrase ''{0}'' in all fields")
  public String exactPhraseInAllFields(String keywords);

  @DefaultMessage("None of the words: {0}; can appear in the fields: {1}")
  public String withoutWordsIn(String keywords, String fields);

  @DefaultMessage("None of the words: {0}; can appear any of the fields")
  public String withoutWordsInAllFields(String keywords);

  @DefaultMessage("{0}, {1}")
  public String listSeparatorComma(String list, String item);

  @DefaultMessage("{0} and {1}")
  public String listSeparatorAnd(String list, String item);

  @DefaultMessage("{0} or {1}")
  public String listSeparatorOr(String list, String item);

}
