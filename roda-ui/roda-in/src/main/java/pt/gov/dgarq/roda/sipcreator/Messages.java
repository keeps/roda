package pt.gov.dgarq.roda.sipcreator;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Message translation resource proxy
 * 
 * @author Luis Faria
 * 
 */
public class Messages {
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME, SIPCreatorConfig.getInstance().getLocale());

	private Messages() {
	}

	/**
	 * Get message
	 * 
	 * @param key
	 *            message key
	 * @return the message
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Get a string array from messages
	 * 
	 * @param key
	 * @return an array of strings
	 */
	public static String[] getStringArray(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key).split(", ");
		} catch (MissingResourceException e) {
			return null;
		}
	}

	/**
	 * Get message
	 * 
	 * @param key
	 *            message key
	 * @param args
	 *            the message arguments
	 * @return the translated message
	 */
	public static String getString(String key, Object... args) {
		return String.format(getString(key), args);
	}

	/**
	 * Get keys that start with prefix
	 * 
	 * @param prefix
	 * @return a set of keys
	 */
	public static Set<String> getKeys(String prefix) {
		Set<String> keys = new LinkedHashSet<String>();
		Enumeration<String> allKeys = RESOURCE_BUNDLE.getKeys();
		while (allKeys.hasMoreElements()) {
			String key = allKeys.nextElement();
			if (key.startsWith(prefix)) {
				keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * Get pairs that start the prefix
	 * 
	 * @param prefix
	 * @return a map with the pairs
	 */
	public static Map<String, String> getMessagesSubSet(String prefix) {
		Map<String, String> subSet = new LinkedHashMap<String, String>();
		for (String key : getKeys(prefix)) {
			String value = getString(key);
			String subKey = key.substring(prefix.length());
			subSet.put(subKey, value);
		}

		return subSet;
	}
}
