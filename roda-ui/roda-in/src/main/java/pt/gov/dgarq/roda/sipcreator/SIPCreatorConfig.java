/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * @author Luis Faria
 * 
 */
public class SIPCreatorConfig {
	private static SIPCreatorConfig instance = null;

	/**
	 * Get SIP Creator Configuration singleton
	 * 
	 * @return the singleton
	 */
	public static SIPCreatorConfig getInstance() {
		if (instance == null) {
			instance = new SIPCreatorConfig();
		}
		return instance;
	}

	private final Logger logger = Logger.getLogger(SIPCreatorConfig.class);

	private Configuration configuration;

	private File sipSentDir;
	private File sipDraftDir;
	private File sipOutboxDir;
	private File tmpDir;
	private File eadDir;
	private URL rodaCoreServices;
	private URL updateCheckURL;
	private URL updateDownloadURL;
	private String version;
	private String buildCode;
	private String buildDate;
	private Locale locale;
	
	private String casURL;

	private SIPCreatorConfig() {
		CompositeConfiguration config = new CompositeConfiguration();
		try {
			if (System.getenv("RODA_IN_HOME") != null) {
				config.addConfiguration(new PropertiesConfiguration(System
						.getenv("RODA_IN_HOME")
						+ File.separator
						+ "config"
						+ File.separator + "roda-in.properties"));
				config.addConfiguration(new PropertiesConfiguration(System
						.getenv("RODA_IN_HOME")
						+ File.separator
						+ "config"
						+ File.separator + "roda-in-version.properties"));
			}else{
				config.addConfiguration(new PropertiesConfiguration(
						"roda-in.properties"));
				config.addConfiguration(new PropertiesConfiguration(
						"roda-in-version.properties"));
			}
			configuration = config;
			sipSentDir = createWriteDir(configuration.getString("sip.sent"));
			sipDraftDir = createWriteDir(configuration.getString("sip.draft"));
			sipOutboxDir = createWriteDir(configuration.getString("sip.outbox"));
			tmpDir = createWriteDir(configuration.getString("tmp"));
			eadDir = createWriteDir(configuration.getString("ead"));
			rodaCoreServices = new URL(configuration
					.getString("roda.services.url"));
			updateCheckURL = new URL(configuration
					.getString("roda.in.update.check.url"));
			updateDownloadURL = new URL(configuration
					.getString("roda.in.update.download.url"));
			version = configuration.getString("roda.in.version");
			buildCode = configuration.getString("roda.in.build.code");
			buildDate = configuration.getString("roda.in.build.date");
			locale = new Locale(configuration.getString("locale"));
			casURL = configuration.getString("roda.cas.url");
		} catch (ConfigurationException e) {
			logger.error("Error getting configuration", e);
			configuration = null;
		} catch (MalformedURLException e) {
			logger.error("Error getting configuration", e);
			configuration = null;
		}
	}


	public URL getCasURL() throws MalformedURLException {
		return new URL(casURL);
	}

	private File createWriteDir(String path) {
		File file = getAbsoluteFile(path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				logger.error("Configuration error: could not create "
						+ file.getAbsolutePath());
			}
		} else {
			if (!file.isDirectory()) {
				logger.error("Configuration error: " + file.getAbsolutePath()
						+ " is not a directory");
			}

			if (!file.canWrite()) {
				logger.error("Configuration error: "
						+ "user does not have write permission in "
						+ file.getAbsolutePath());
			}
		}
		return file;
	}

	private File getAbsoluteFile(String path) {
		File pathFile = new File(path);
		File absoluteFile;
		if (pathFile.isAbsolute()) {
			absoluteFile = pathFile;
		} else {
			// Set absolute file as relative to user.dir
			logger.debug("Path " + path + " is relative, setting base dir="
					+ System.getProperty("user.home"));
			absoluteFile = new File(System.getProperty("user.home"), path);
		}
		return absoluteFile;
	}

	/**
	 * Get SIP sent directory
	 * 
	 * @return the SIP sent directory
	 */
	public File getSipSentDir() {
		return sipSentDir;
	}

	/**
	 * Get SIP draft directory
	 * 
	 * @return The SIP draft directory
	 */
	public File getSipDraftDir() {
		return sipDraftDir;
	}

	/**
	 * Get the temporary files directory
	 * 
	 * @return the temporary files directory
	 */
	public File getTmpDir() {
		return tmpDir;
	}

	/**
	 * Get the SIP Outbox directory
	 * 
	 * @return the SIP Outbox directory
	 */
	public File getSipOutboxDir() {
		return sipOutboxDir;
	}

	/**
	 * Get the classification plan directory
	 * 
	 * @return the classification plan directory
	 */
	public File getEadDir() {
		return eadDir;
	}

	/**
	 * Get RODA Core Services URL
	 * 
	 * @return the RODA Core Services URL
	 */
	public URL getRODACoreServices() {
		return rodaCoreServices;
	}


	/**
	 * Get the update check URL
	 * 
	 * @return an URL that points to the last version of
	 *         roda-in-version.properties
	 */
	public URL getUpdateCheckUrl() {
		return updateCheckURL;
	}

	/**
	 * Get the update download URL
	 * 
	 * @return an URL that points to the last version of the RODA-in installer
	 */
	public URL getUpdateDownloadUrl() {
		return updateDownloadURL;
	}

	/**
	 * Get RODA-in version
	 * 
	 * @return the version string
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Get RODA-in build code
	 * 
	 * @return the build code
	 */
	public String getBuildCode() {
		return buildCode;
	}

	/**
	 * Get RODA-in build date
	 * 
	 * @return the build date, in ISO 8601
	 */
	public String getBuildDate() {
		return buildDate;
	}

	/**
	 * Get defined locale
	 * 
	 * @return the locale defined in properties
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Update interface
	 * 
	 */
	public interface UpdateInterface {
		/**
		 * Called when application is outdated
		 * 
		 * @param newVersion
		 *            the new version number
		 * @param oldVersion
		 *            the old version number
		 */
		public void onOutdated(String newVersion, String oldVersion);

		/**
		 * Called when application is updated
		 * 
		 * @param version
		 *            the updated version number
		 */
		public void onUpdated(String version);
	}

	/**
	 * Check for update in a different thread and, if there is a new version,
	 * run argument runnable
	 * 
	 * @param updateInterface
	 */
	public void checkForUpdate(final UpdateInterface updateInterface) {
		Thread updateThread = new Thread() {

			public void run() {
				Properties lastVersion = new Properties();
				try {
					logger.debug("Fetching last version");
					lastVersion.load(getUpdateCheckUrl().openStream());
					logger.debug("Got version, checking against own version");
					String newVersion = lastVersion
							.getProperty("roda.in.version");
					if (compareVersion(newVersion, getVersion()) > 0) {
						logger.debug("Is outdated " + newVersion + "!="
								+ getVersion());
						updateInterface.onOutdated(newVersion, getVersion());
					} else {
						logger.debug("Is updated " + newVersion + "=="
								+ getVersion());
						updateInterface.onUpdated(getVersion());
					}
				} catch (IOException e) {
					logger.debug("Could not get last version from server", e);
				}
			}

		};

		updateThread.start();
	}

	private int compareVersion(String version1, String version2) {
		String[] split1 = version1.split("\\.");
		String[] split2 = version2.split("\\.");
		int compare = 0;
		for (int i = 0; i < split1.length && i < split2.length; i++) {
			int sub_version1 = Integer.valueOf(split1[i]);
			int sub_version2 = Integer.valueOf(split2[i]);
			if (sub_version1 != sub_version2) {
				compare = sub_version1 - sub_version2;
				break;
			}
		}

		if (compare == 0 && split1.length != split2.length) {
			compare = split1.length - split2.length;
		}

		return compare;
	}

}
