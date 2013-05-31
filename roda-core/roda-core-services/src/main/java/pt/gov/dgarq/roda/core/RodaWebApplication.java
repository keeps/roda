package pt.gov.dgarq.roda.core;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.ingest.IngestManager;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;
import pt.gov.dgarq.roda.core.scheduler.RODASchedulerException;
import pt.gov.dgarq.roda.core.scheduler.SchedulerManager;
import pt.gov.dgarq.roda.core.services.UserBrowser;

/**
 * This class listens for
 * {@link RodaWebApplication#contextInitialized(ServletContextEvent)} and
 * {@link RodaWebApplication#contextDestroyed(ServletContextEvent)} events in
 * RODA Core servlet.
 * 
 * @author Rui Castro
 */
public class RodaWebApplication implements ServletContextListener {

	public static File RODA_HOME = null;
	public static File RODA_CORE_CONFIG_DIRECTORY = null;

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

			RODA_CORE_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

			File log4jXml = new File(RODA_CORE_CONFIG_DIRECTORY, "log4j.xml");
			DOMConfigurator.configure(log4jXml.getPath());

		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}

		logger = Logger.getLogger(RodaWebApplication.class);
	}

	private SchedulerManager scheduler = null;

	private PluginManager pluginManager = null;

	private IngestManager ingestManager = null;

	/**
	 * 
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent ctx) {

		logger.info("RODA Core starting");

		try {

			logger.debug(getClass().getName() + " ClassLoader is "
					+ getClass().getClassLoader());

			try {

				this.ingestManager = IngestManager.getDefaultIngestManager();

			} catch (IngestRegistryException e) {
				logger.error(
						"Error creating Ingest Manager - " + e.getMessage(), e);
			}

			try {

				UserBrowser userBrowser = new UserBrowser();
				User[] users = userBrowser.getUsers(null);

				this.ingestManager.createFTPDropDirectories(users);

			} catch (RODAServiceException e) {
				logger.error(
						"Error getting list of users - "
								+ e.getMessage()
								+ ". FTP drop directories will not be verified/created.",
						e);
			}

			// IMPORTANT: It's very important that
			// 'ingestManager.clearProcessingFlags()' is called BEFORE the
			// Scheduler
			// is started, because no ingest tasks should be running at this
			// stage.
			try {

				this.ingestManager.clearProcessingFlags();

			} catch (IngestRegistryException e) {
				logger.error(
						"Error clearing processing flags - "
								+ e.getMessage()
								+ ". It's possible that some SIPs stay blocked.",
						e);
			}

			try {

				// Create the PluginManager
				this.pluginManager = PluginManager.getDefaultPluginManager();

			} catch (PluginManagerException e) {
				logger.error(
						"Error creating Plugin Manager - " + e.getMessage(), e);
			}

			try {

				scheduler = SchedulerManager.getDefaultSchedulerManager();

			} catch (RODASchedulerException e) {
				logger.error(
						"Error getting default RODA Scheduler - "
								+ e.getMessage(), e);
			}

			logger.info("RODA Core started");

		} catch (Throwable t) {
			logger.error("Error starting RODA Core - " + t.getMessage(), t);
		}

	}

	/**
	 * 
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent ctx) {

		logger.info("RODA Core stopping");

		// Shutdown PluginManager
		if (this.pluginManager != null) {
			this.pluginManager.shutdown();
			this.pluginManager = null;
		}

		if (this.scheduler != null) {

			try {

				this.scheduler.stop();
				this.scheduler = null;

			} catch (RODASchedulerException e) {
				logger.error(
						"Error stopping RODA Scheduler - " + e.getMessage(), e);
			}

		}

		logger.info("RODA Core stopped");
	}

	/**
	 * Return the configuration properties with the specified name.
	 * 
	 * @param c
	 *            the {@link Class} for which the properties will be loaded.
	 * @param configurationFile
	 *            the name of the configuration file.
	 * 
	 * @return a {@link Configuration} with the properties of the specified
	 *         name.
	 * 
	 * @throws ConfigurationException
	 */
	public static Configuration getConfiguration(Class<?> c,
			String configurationFile) throws ConfigurationException {

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(
				RodaWebApplication.RODA_CORE_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration
					.load(c.getResource("/" + configurationFile));
			logger.debug("Loading default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

}
