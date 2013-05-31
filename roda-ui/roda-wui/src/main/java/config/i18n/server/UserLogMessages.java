/**
 * 
 */
package config.i18n.server;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Luis Faria
 *
 */
public class UserLogMessages {
	private static final String BUNDLE_NAME = "config.i18n.server.UserLogMessages"; //$NON-NLS-1$

	private ResourceBundle resourceBundle;

	/**
	 * Create a new user log list report messages translations for a defined
	 * locale
	 * 
	 * @param locale
	 */
	public UserLogMessages(Locale locale) {
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
}
