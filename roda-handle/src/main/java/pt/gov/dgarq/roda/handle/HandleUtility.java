package pt.gov.dgarq.roda.handle;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 */
public class HandleUtility {

	static final private Logger logger = Logger.getLogger(HandleUtility.class);

	private static String handleURLBase = null;
	private static String rodaHandlePrefix = null;

	private static String rodaNAHandleURL = null;

	static {
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		try {

			configuration.load(HandleUtility.class
					.getResource("/roda-handle.properties"));

			handleURLBase = configuration.getString("handleURLBase");
			rodaHandlePrefix = configuration.getString("rodaHandlePrefix");

		} catch (ConfigurationException e) {
			logger.error(
					"Error reading configuration file - roda-handle.properties - "
							+ e.getMessage(), e);
		}

		rodaNAHandleURL = handleURLBase + rodaHandlePrefix;

		logger.info("init OK");
	}

	/**
	 * Returns the handle URL for the given PID.
	 * 
	 * @param doPID
	 *            the Description Object PID.
	 * 
	 * @return a {@link String} with the handle URL for the given PID.
	 */
	public static String getHandleURLForPID(String doPID) {
		String[] pidParts = doPID.split(":");
		return rodaNAHandleURL + "/" + pidParts[1];
	}

}
