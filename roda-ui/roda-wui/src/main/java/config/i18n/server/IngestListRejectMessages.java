package config.i18n.server;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 
 * @author Luis Faria
 * 
 */
public class IngestListRejectMessages {
	private static final String BUNDLE_NAME = "config.i18n.server.IngestListRejectMessages"; //$NON-NLS-1$

	private ResourceBundle resourceBundle;

	/**
	 * Create a new ingest list report messages translations for a defined
	 * locale
	 * 
	 * @param locale
	 */
	public IngestListRejectMessages(Locale locale) {
		this.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}

	/**
	 * Get translation
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return resourceBundle.getString(key);
	}

	public Map<String, String> getMessages() {
		Map<String, String> messages = new HashMap<String, String>();
		Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = resourceBundle.getString(key);
			messages.put(key, value);
		}
		return messages;
	}

}
