package pt.gov.dgarq.roda.core.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.util.ClassLoaderUtility;

/**
 * This is the RODA plugin manager. It is responsible for loading {@link Plugin}
 * s.
 * 
 * @author Rui Castro
 */
public class PluginManager {
	private static Logger logger = Logger.getLogger(PluginManager.class);

	public static File RODA_HOME = null;
	public static File RODA_CORE_CONFIG_DIRECTORY = null;

	private static String RODA_PLUGIN_MANIFEST_KEY = "RODA-Plugin";

	/**
	 * The default Plugin Manager instance.
	 */
	private static PluginManager defaultPluginManager = null;

	/**
	 * Gets the default {@link PluginManager}.
	 * 
	 * @return the default {@link PluginManager}.
	 * 
	 * @throws PluginManagerException
	 */
	public synchronized static PluginManager getDefaultPluginManager()
			throws PluginManagerException {
		if (defaultPluginManager == null) {
			defaultPluginManager = new PluginManager();
		}
		return defaultPluginManager;
	}

	private File pluginsDirectory = null;

	private Timer loadPluginsTimer = null;

	private Map<File, JarPlugin> jarPluginCache = new HashMap<File, JarPlugin>();

	/**
	 * Returns all {@link Plugin}s present in all jars.
	 * 
	 * @return a {@link List} of {@link Plugin}s.
	 */
	public List<Plugin> getPlugins() {
		List<Plugin> plugins = new ArrayList<Plugin>();

		for (JarPlugin jarPlugin : this.jarPluginCache.values()) {
			if (jarPlugin.plugin != null) {
				plugins.add(jarPlugin.plugin);
			}
		}
		return plugins;
	}

	/**
	 * Returns the {@link PluginInfo}s for all {@link Plugin}s.
	 * 
	 * @return a {@link List} of {@link PluginInfo}s.
	 */
	public List<PluginInfo> getPluginsInfo() {
		List<PluginInfo> pluginsInfo = new ArrayList<PluginInfo>();

		for (Plugin plugin : getPlugins()) {
			pluginsInfo.add(getPluginInfo(plugin));
		}

		return pluginsInfo;
	}

	/**
	 * Returns an instance of the {@link Plugin} with the specified ID
	 * (classname).
	 * 
	 * @param pluginID
	 *            the ID (classname) of the {@link Plugin}.
	 * 
	 * @return a {@link Plugin} or <code>null</code> if the specified classname
	 *         if not a {@link Plugin}.
	 */
	public Plugin getPlugin(String pluginID) {
		Plugin plugin = null;
		for (JarPlugin jarPlugin : this.jarPluginCache.values()) {
			if (jarPlugin.plugin != null
					&& jarPlugin.plugin.getClass().getName().equals(pluginID)) {
				plugin = jarPlugin.plugin;
			}
		}
		return plugin;
	}

	/**
	 * @param pluginID
	 * 
	 * @return {@link PluginInfo} or <code>null</code>.
	 */
	public PluginInfo getPluginInfo(String pluginID) {
		Plugin plugin = getPlugin(pluginID);
		if (plugin != null) {
			return getPluginInfo(plugin);
		} else {
			return null;
		}
	}

	/**
	 * This method should be called to stop {@link PluginManager} and all
	 * {@link Plugin}s currently loaded.
	 */
	public void shutdown() {

		if (this.loadPluginsTimer != null) {
			// Stop the plugin loader timer
			this.loadPluginsTimer.cancel();
		}

		for (JarPlugin jarPlugin : this.jarPluginCache.values()) {
			if (jarPlugin.plugin != null) {
				jarPlugin.plugin.shutdown();
			}
		}
	}

	/**
	 * Constructs a new {@link PluginManager}.
	 * 
	 * @throws PluginManagerException
	 */
	private PluginManager() throws PluginManagerException {

		if (System.getProperty("roda.home") != null) {
			RODA_HOME = new File(System.getProperty("roda.home"));
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
		} else {
			RODA_HOME = new File(".");
		}

		RODA_CORE_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

		try {

			Configuration configuration = getConfiguration("plugins.properties");

			String pluginDirectory = configuration
					.getString("pluginsDirectory");

			File pluginDirectoryFile = new File(pluginDirectory);

			logger.debug("Plugin directory is " + pluginDirectoryFile);

			setPluginDirectory(pluginDirectoryFile);

		} catch (ConfigurationException e) {
			logger.debug(
					"Error reading plugins.properties - " + e.getMessage(), e);
			throw new PluginManagerException(
					"Error reading plugins.properties - " + e.getMessage(), e);
		} catch (Throwable t) {
			logger.error("Error creating PluginManager - " + t.getMessage(), t);
			throw new PluginManagerException("Error reading PluginManager - "
					+ t.getMessage(), t);
		}

		logger.debug("Starting plugin scanner timer...");

		this.loadPluginsTimer = new Timer("Plugin scanner timer", true);
		this.loadPluginsTimer.schedule(new SearchPluginsTask(), new Date(),
				30 * 1000);

		logger.info(getClass().getSimpleName() + " init OK");
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
	private Configuration getConfiguration(String configurationFile)
			throws ConfigurationException {

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(RODA_CORE_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.info("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile));
			logger.info("Loading default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

	/**
	 * @return the pluginDirectory
	 */
	private File getPluginDirectory() {
		return pluginsDirectory;
	}

	/**
	 * @param pluginDirectory
	 *            the pluginDirectory to set
	 * 
	 * @throws NullPointerException
	 *             if pluginDirectory is null.
	 * @throws IllegalArgumentException
	 *             if pluginDirectory is not a directory.
	 */
	private void setPluginDirectory(File pluginDirectory)
			throws NullPointerException, IllegalArgumentException {

		if (pluginDirectory == null) {

			throw new NullPointerException("pluginDirectory cannot be null");

		} else if (!pluginDirectory.isDirectory()) {

			throw new IllegalArgumentException("pluginDirectory "
					+ pluginDirectory + " is not a directory.");

		} else {

			this.pluginsDirectory = pluginDirectory;

		}
	}

	private PluginInfo getPluginInfo(Plugin plugin) {
		List<PluginParameter> parameters = plugin.getParameters();
		return new PluginInfo(plugin.getClass().getName(), plugin.getName(),
				plugin.getVersion(), plugin.getDescription(),
				parameters.toArray(new PluginParameter[parameters.size()]));
	}

	private void loadPlugins() {

		File jarFiles[] = getPluginDirectory().listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});

		URL[] jarURLs = new URL[jarFiles.length];
		for (int i = 0; i < jarFiles.length; i++) {

			try {

				jarURLs[i] = jarFiles[i].toURI().toURL();

			} catch (MalformedURLException e) {
				logger.warn("Error getting jar file URL - " + e.getMessage(), e);
			}
		}

		for (File jarFile : jarFiles) {

			if (jarPluginCache.containsKey(jarFile)
					&& jarFile.lastModified() == jarPluginCache.get(jarFile).lastModified) {
				// The plugin already exists

				logger.debug(jarFile.getName() + " is already loaded");

			} else {
				// The plugin doesn't exist or the modification date is
				// different. Let's load the Plugin

				logger.debug(jarFile.getName()
						+ " is not loaded or modification dates differ. Inspecting Jar...");

				Plugin plugin = loadPlugin(jarFile, jarURLs);

				try {
					if (plugin != null) {

						plugin.init();
						logger.debug("Plugin started " + plugin.getName()
								+ " (version " + plugin.getVersion() + ")");

					} else {

						logger.trace(jarFile.getName() + " is not a Plugin");

					}

					synchronized (jarPluginCache) {
						jarPluginCache.put(jarFile, new JarPlugin(plugin,
								jarFile.lastModified()));
					}

				} catch (PluginException e) {
					logger.error("Plugin failed to initialize", e);
				} catch (Exception e) {
					logger.error("Plugin failed to initialize", e);
				}
			}

		}

	}

	private Plugin loadPlugin(File jarFile, URL[] jarURLs) {

		JarFile jar = null;
		Plugin plugin = null;

		try {

			jar = new JarFile(jarFile);

			Manifest manifest = jar.getManifest();

			if (manifest == null) {
				logger.trace(jarFile.getName()
						+ " doesn't have a MANIFEST file");
			} else {

				Attributes mainAttributes = manifest.getMainAttributes();

				String pluginClassName = mainAttributes
						.getValue(RODA_PLUGIN_MANIFEST_KEY);

				if (pluginClassName != null) {

					logger.trace(jarFile.getName() + " has plugin "
							+ pluginClassName);

					logger.trace("Adding jar " + jarFile.getName()
							+ " to classpath and loading " + pluginClassName
							+ " with ClassLoader "
							+ URLClassLoader.class.getSimpleName());

					Object object = ClassLoaderUtility.createObject(jarURLs,
							pluginClassName);

					if (Plugin.class.isAssignableFrom(object.getClass())) {

						plugin = (Plugin) object;

					} else {
						logger.error(pluginClassName + " is not a valid Plugin");
					}

				} else {
					logger.trace(jarFile.getName()
							+ " MANIFEST file doesn't have a '"
							+ RODA_PLUGIN_MANIFEST_KEY + "' attribute");
				}

			}

		} catch (Throwable e) {

			logger.error(
					jarFile.getName() + " error loading plugin - "
							+ e.getMessage(), e);

		} finally {

			if (jar != null) {

				try {
					jar.close();
				} catch (IOException e) {
					logger.debug("Error closing jar - " + e.getMessage(), e);
				}
			}
		}

		return plugin;
	}

	class SearchPluginsTask extends TimerTask {

		public void run() {

			logger.debug("Searching for plugins...");

			loadPlugins();

			logger.debug("Search complete - " + jarPluginCache.size()
					+ " jar files");

			for (File jarFile : jarPluginCache.keySet()) {

				Plugin plugin = jarPluginCache.get(jarFile).plugin;

				if (plugin != null) {
					logger.debug("- " + jarFile.getName());
					logger.debug("--- " + plugin.getName() + "-"
							+ plugin.getVersion());
				}
			}
		}
	}

	class JarPlugin {

		Plugin plugin = null;
		long lastModified = 0;

		JarPlugin(Plugin plugin, long lastModified) {
			this.plugin = plugin;
			this.lastModified = lastModified;
		}
	}

}
