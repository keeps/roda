package pt.gov.dgarq.roda.migrator;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class listens for
 * {@link RodaMigratorApplication#contextInitialized(ServletContextEvent)} and
 * {@link RodaMigratorApplication#contextDestroyed(ServletContextEvent)} events
 * in RODA Migrator Cache Servlet.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public class RodaMigratorApplication implements ServletContextListener {

	public static File RODA_HOME = null;
	public static File RODA_MIGRATOR_CONFIG_DIRECTORY = null;

	private static final Logger logger;
	static {
		try {

			if (System.getProperty("roda.home") != null) {
				RODA_HOME = new File(System.getProperty("roda.home"));
			} else if (System.getenv("RODA_HOME") != null) {
				RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
			} else {
				RODA_HOME = new File(".");
			}

			RODA_MIGRATOR_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$
			
			File log4jXml = new File(RODA_MIGRATOR_CONFIG_DIRECTORY,
					"migrator-log4j.xml");
			DOMConfigurator.configure(log4jXml.getPath());

		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}

		logger = Logger.getLogger(RodaMigratorApplication.class);
	}

	private static Configuration configuration = null;

	private File cacheDirectory = null;

	/**
	 * 
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {

		logger.info("RODA Migrator starting");

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
		if (this.cacheDirectory.exists()) {

			if (FileUtils.deleteQuietly(this.cacheDirectory)) {
				logger.info("Existing cache directory deleted");
			} else {
				logger.warn("Error deleting cache directory");
			}

		}

		if (this.cacheDirectory.mkdir()) {
			logger.info("Cache directory created");
		} else {
			logger.warn("Error creating cache directory");
		}

		// Initialize singletons
		// ServletContext ctx = event.getServletContext();
		// Unoconv.createInstance(ctx);

		logger.info("RODA Migrator started");

	}

	/**
	 * 
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {

		// Destroy singletons
		// ServletContext ctx = event.getServletContext();
		// Unoconv.destroyInstance(ctx);

		logger.info("RODA Migrator stopped");
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

		File externalConfigurationFile = new File(
				RODA_MIGRATOR_CONFIG_DIRECTORY, configurationFile);

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
