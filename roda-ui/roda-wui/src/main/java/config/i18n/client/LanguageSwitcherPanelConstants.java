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
  @DefaultStringValue("Portuguese")
  public String lang_pt();

  @Key("lang.cz")
  @DefaultStringValue("Czech")
  public String lang_cz();
}