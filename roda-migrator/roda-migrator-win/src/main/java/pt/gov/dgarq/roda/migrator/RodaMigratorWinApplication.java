package pt.gov.dgarq.roda.migrator;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class listens for
 * {@link RodaMigratorWinApplication#contextInitialized(ServletContextEvent)}
 * and {@link RodaMigratorWinApplication#contextDestroyed(ServletContextEvent)}
 * events in RODA Migrator Win Cache Servlet.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public class RodaMigratorWinApplication implements ServletContextListener {

	private static final Logger logger = Logger
			.getLogger(RodaMigratorWinApplication.class);

	private File roda_config_directory;

	private static Configuration configuration = null;

	private File cacheDirectory = null;

	/**
	 * 
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {

		logger.info("RODA Migrator Win starting");
		String roda_home;
		if (System.getProperty("roda.home") != null) {
			roda_home = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			roda_home = System.getenv("RODA_HOME");
		} else {
			roda_home = "c:\\roda";
		}
		roda_config_directory = new File(roda_home, "config"); //$NON-NLS-1$

		try {

			if (configuration == null) {
				configuration = getConfiguration("roda-migrator.properties");
			}

			cacheDirectory = new File(configuration.getString("cacheDirectory"));

		} catch (ConfigurationException e) {

			logger.error("Error reading roda-migrator.properties - "
					+ e.getMessage(), e);

		}

		// Clean cache directory

		logger.info("Cleaning cache directory (" + this.cacheDirectory
				+ ") at startup");

		if (this.cacheDirectory.exists()) {

			if (FileUtils.deleteQuietly(this.cacheDirectory)) {
				logger.info("Existing cache directory deleted ("
						+ this.cacheDirectory + ")");
			} else {
				logger.warn("Error deleting cache directory");
			}

		}

		if (this.cacheDirectory.mkdir()) {
			logger
					.info("Cache directory created (" + this.cacheDirectory
							+ ")");
		} else {
			logger.warn("Error creating cache directory");
		}

		logger.info("RODA Migrator Win started");

	}

	/**
	 * 
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
		logger.info("RODA Migrator Win stopped");
	}

	/**
	 * Return the configuration properties with the specified name.
	 * 
	 * @param configurationFile
	 *            the name of the configuration file.
	 * 
	 * @return a {@link Configuration} with the properties of the specified
	 *         name.
	 * 
	 * @throws ConfigurationException
	 */
	public Configuration getConfiguration(String configurationFile)
			throws ConfigurationException {

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(roda_config_directory,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile));
			logger.debug("Loading default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

}
