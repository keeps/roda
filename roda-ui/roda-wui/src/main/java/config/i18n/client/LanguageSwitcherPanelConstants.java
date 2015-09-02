/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants.DefaultStringValue;
import com.google.gwt.i18n.client.Messages;

public interface LanguageSwitcherPanelConstants extends Messages{
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