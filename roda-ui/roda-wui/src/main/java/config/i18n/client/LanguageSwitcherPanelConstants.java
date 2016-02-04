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
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface LanguageSwitcherPanelConstants extends Constants, ConstantsWithLookup {
  @Key("lang.en")
  @DefaultStringValue("English")
  public String lang_en();

  @Key("lang.pt")
  @DefaultStringValue("Português")
  public String lang_pt();

  @Key("lang.cz")
  @DefaultStringValue("Čeština")
  public String lang_cz();
}