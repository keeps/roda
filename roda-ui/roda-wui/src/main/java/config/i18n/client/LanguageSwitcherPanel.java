/**
 * 
 */
package config.i18n.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import config.i18n.client.flags.FlagsBundle;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class LanguageSwitcherPanel extends Composite {

	private static final FlagsBundle flags = (FlagsBundle) GWT
			.create(FlagsBundle.class);

	private final Map<String, Image> languages;

	private final FlowPanel layout;

	public LanguageSwitcherPanel() {
		languages = new HashMap<String, Image>();
		layout = new FlowPanel();
		initWidget(layout);

		// insert new languages here
		languages.put("pt_PT", new Image(flags.pt_PT()));
		languages.put("en", new Image(flags.en()));
		languages.put("cs_CZ", new Image(flags.cs_CZ()));

		// Adding all defined languages
		for (Entry<String, Image> entry : languages.entrySet()) {
			addLanguage(entry.getKey(), entry.getValue());
		}

		layout.addStyleName("wui-languageSwitcher");
	}

	/**
	 * Add a new language
	 * 
	 * @param locale
	 *            the language locale, e.g. en_US
	 * @param flag
	 *            the flag image to add to panel
	 */
	public void addLanguage(final String locale, Image flag) {
		layout.add(flag);
		flag.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeLocale(locale);
			}
		});

		flag.addStyleName("languageSwitcher-flag");
	}

	/**
	 * JSNI method to change the locale of the application - it effectively
	 * parses the existing URL and creates a new one for the chosen locale.
	 * 
	 * 
	 * @param newLocale
	 */
	private native void changeLocale(String newLocale)/*-{
														var currLocation = $wnd.location.toString(); 
														var noHistoryCurrLocArray = currLocation.split("#"); 
														var noHistoryCurrLoc = noHistoryCurrLocArray[0]; 
														var locArray = noHistoryCurrLoc.split("?"); 
														$wnd.location.href = locArray[0]+"?locale="+newLocale+"#"+noHistoryCurrLocArray[1];
														}-*/;
}
