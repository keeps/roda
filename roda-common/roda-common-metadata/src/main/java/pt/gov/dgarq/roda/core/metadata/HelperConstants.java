package pt.gov.dgarq.roda.core.metadata;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class HelperConstants {
	private static final String BUNDLE_NAME = "pt.gov.dgarq.roda.core.metadata.helperConstants";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private HelperConstants() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
