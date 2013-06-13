package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.ingest.IngestManager;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;
import pt.gov.dgarq.roda.core.scheduler.RODASchedulerException;
import pt.gov.dgarq.roda.core.scheduler.SchedulerManager;
import pt.gov.dgarq.roda.core.services.UserBrowser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	public static final String RODA_CORE_POSTINSTALL_FILENAME = "roda-core.postinstall";
	public static final String RODA_CORE_PROPERTIES_FILENAME = "roda-core.properties";

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

			// create default tasks the first time RODA-CORE is
			// initialized
			Task task = null;
			if (scheduler != null && createDefaultTasks()) {
				Configuration rodaCoreConfiguration = getConfiguration(
						this.getClass(), RODA_CORE_PROPERTIES_FILENAME);
				if (rodaCoreConfiguration != null) {
					List<String> defaultTasks = rodaCoreConfiguration
							.getList("defaultTasks");
					for (String taskConfiguration : defaultTasks) {
						task = createTask(taskConfiguration);
						if (task != null) {
							scheduler.addTask(task);
						}
					}
					if (!createRODACorePostInstallFile()) {
						logger.error("Error creating file \""
								+ RODA_CORE_POSTINSTALL_FILENAME + "\"...");
					}
				}
			}
			logger.info("RODA Core started");

		} catch (Throwable t) {
			logger.error("Error starting RODA Core - " + t.getMessage(), t);
		}

	}

	/**
	 * Method that determines if default tasks should be created. It checks if a
	 * file called {@value #RODA_CORE_POSTINSTALL_FILENAME} exists under RODA
	 * config directory. If it doesn't exists, create default tasks. Otherwise,
	 * don't.
	 * 
	 * @return true if default tasks should be create and false otherwise.
	 * */
	private boolean createDefaultTasks() {
		File rodaCorePostInstall = new File(RODA_CORE_CONFIG_DIRECTORY,
				RODA_CORE_POSTINSTALL_FILENAME);
		return !rodaCorePostInstall.exists();
	}

	/**
	 * Method that creates a file called
	 * {@value #RODA_CORE_POSTINSTALL_FILENAME} under RODA config directory to
	 * mark the execution of post install tasks (things that should only be done
	 * the first time RODA is initialized)
	 * 
	 * @return true if the file was successfully create and false otherwise
	 * 
	 * */
	private boolean createRODACorePostInstallFile() {
		boolean res = true;
		File rodaCorePostInstall = new File(RODA_CORE_CONFIG_DIRECTORY,
				RODA_CORE_POSTINSTALL_FILENAME);
		try {
			res = rodaCorePostInstall.createNewFile();
		} catch (IOException e) {
			res = false;
		}
		return res;
	}

	/**
	 * Method that creates a task for a given JSON configuration, obtained from
	 * roda-core.properties
	 * 
	 * @return a {@link Task} if everything goes alright or null otherwise
	 * 
	 * */
	private Task createTask(String pluginConfiguration) {
		Task task = new Task();
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		HashMap<String, Object> jsonObject;
		String value;
		try {
			jsonObject = mapper.readValue(pluginConfiguration, typeRef);
			value = (String) jsonObject.get("name");
			task.setName(value);
			task.setRepeatCount(-1);
			value = (String) jsonObject.get("interval");
			task.setRepeatInterval(Long.valueOf(value));
			task.setUsername("admin");
			task.setStartDate(new Date());
			String pluginName = (String) jsonObject.get("plugin");
			PluginInfo pluginInfo = null;
			int retries = 3, sleep = 1, i = 0;
			while (i < retries) {
				pluginInfo = pluginManager.getPluginInfo(pluginName);
				if (pluginInfo == null) {
					i++;
					try {
						Thread.sleep(1000 * i * sleep);
					} catch (InterruptedException e) {
					}
				} else {
					PluginParameter[] parameters = pluginInfo.getParameters();
					for (PluginParameter param : parameters) {
						value = (String) jsonObject.get(param.getName());
						param.setValue(value);
					}
					task.setPluginInfo(pluginInfo);
					break;
				}
			}
			if (pluginInfo == null) {
				task = null;
			}
		} catch (JsonParseException e) {
			logger.error(e);
			task = null;
		} catch (JsonMappingException e) {
			logger.error(e);
			task = null;
		} catch (IOException e) {
			logger.error(e);
			task = null;
		}
		return task;
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
