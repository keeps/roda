/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.reflections.Reflections;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.PluginInfo;
import org.roda.core.util.ClassLoaderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the RODA plugin manager. It is responsible for loading {@link Plugin}
 * s.
 * 
 * @author Rui Castro
 */
public class PluginManager {
  private static Logger logger = LoggerFactory.getLogger(PluginManager.class);

  private static Path RODA_CONFIG_PATH = null;
  private static Path RODA_PLUGINS_PATH = null;

  private static String RODA_PLUGIN_MANIFEST_KEY = "RODA-Plugin";

  private Timer loadPluginsTimer = null;
  private Map<Path, JarPlugin> jarPluginCache = new HashMap<Path, JarPlugin>();
  private Map<String, Plugin<?>> internalPluginChache = new HashMap<String, Plugin<?>>();

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
  public synchronized static PluginManager getDefaultPluginManager(Path rodaConfigPath, Path rodaPluginsPath)
    throws PluginManagerException {
    if (defaultPluginManager == null) {
      RODA_CONFIG_PATH = rodaConfigPath;
      RODA_PLUGINS_PATH = rodaPluginsPath;
      defaultPluginManager = new PluginManager();
    }
    return defaultPluginManager;
  }

  /**
   * Returns all {@link Plugin}s present in all jars.
   * 
   * @return a {@link List} of {@link Plugin}s.
   */
  public List<Plugin<?>> getPlugins() {
    List<Plugin<?>> plugins = new ArrayList<Plugin<?>>();

    for (JarPlugin jarPlugin : this.jarPluginCache.values()) {
      if (jarPlugin.plugin != null) {
        plugins.add(jarPlugin.plugin);
      }
    }

    plugins.addAll(internalPluginChache.values());

    return plugins;
  }

  /**
   * Returns the {@link PluginInfo}s for all {@link Plugin}s.
   * 
   * @return a {@link List} of {@link PluginInfo}s.
   */
  public List<PluginInfo> getPluginsInfo() {
    List<PluginInfo> pluginsInfo = new ArrayList<PluginInfo>();

    for (Plugin<?> plugin : getPlugins()) {
      pluginsInfo.add(getPluginInfo(plugin));
    }

    return pluginsInfo;
  }

  /**
   * Returns an instance of the {@link Plugin} with the specified ID
   * (classname).
   * 
   * @param pluginID
   *          the ID (classname) of the {@link Plugin}.
   * 
   * @return a {@link Plugin} or <code>null</code> if the specified classname if
   *         not a {@link Plugin}.
   */
  public Plugin<?> getPlugin(String pluginID) {
    Plugin<?> plugin = null;

    if (internalPluginChache.get(pluginID) != null) {
      plugin = internalPluginChache.get(pluginID);
    }

    boolean internalPluginTakesPrecedence = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.internal.take_precedence_over_external");
    if (plugin == null || !internalPluginTakesPrecedence) {

      for (JarPlugin jarPlugin : this.jarPluginCache.values()) {
        if (jarPlugin.plugin != null && jarPlugin.plugin.getClass().getName().equals(pluginID)) {
          plugin = jarPlugin.plugin;
          break;
        }
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
    Plugin<?> plugin = getPlugin(pluginID);
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
    logger.debug("Starting plugin scanner timer...");

    int timeInSeconds = RodaCoreFactory.getRodaConfiguration().getInt("core.plugins.external.scheduler.interval");
    this.loadPluginsTimer = new Timer("Plugin scanner timer", true);
    this.loadPluginsTimer.schedule(new SearchPluginsTask(), new Date(), timeInSeconds * 1000);

    logger.info(getClass().getSimpleName() + " init OK");
  }

  private <T extends Serializable> PluginInfo getPluginInfo(Plugin<T> plugin) {
    return new PluginInfo(plugin.getClass().getName(), plugin.getName(), plugin.getVersion(), plugin.getDescription(),
      plugin.getParameters());
  }

  private void loadPlugins() {
    // load internal RODA plugins
    loadInternalPlugins();

    // load "external" RODA plugins, i.e., those available in the plugins folder
    loadExternalPlugins();

  }

  private void loadExternalPlugins() {
    try {
      List<Path> jarFiles = new ArrayList<>();
      List<URL> jarURLs = new ArrayList<>();
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(RODA_PLUGINS_PATH, "*.jar")) {
        for (Path jarFile : stream) {
          jarFiles.add(jarFile);
          jarURLs.add(jarFile.toUri().toURL());
        }
      }

      for (Path jarFile : jarFiles) {
        BasicFileAttributes attrs = Files.readAttributes(jarFile, BasicFileAttributes.class);

        if (jarPluginCache.containsKey(jarFile)
          && attrs.lastModifiedTime().toMillis() == jarPluginCache.get(jarFile).lastModified) {
          // The plugin already exists

          logger.debug(jarFile.getFileName() + " is already loaded");

        } else {
          // The plugin doesn't exist or the modification date is
          // different. Let's load the Plugin

          logger.debug(jarFile.getFileName() + " is not loaded or modification dates differ. Inspecting Jar...");

          Plugin<?> plugin = loadPlugin(jarFile, jarURLs);

          try {
            if (plugin != null) {

              plugin.init();
              logger.debug("Plugin started " + plugin.getName() + " (version " + plugin.getVersion() + ")");

            } else {

              logger.trace(jarFile.getFileName() + " is not a Plugin");

            }

            synchronized (jarPluginCache) {
              jarPluginCache.put(jarFile, new JarPlugin(plugin,
                Files.readAttributes(jarFile, BasicFileAttributes.class).lastModifiedTime().toMillis()));
            }

          } catch (PluginException e) {
            logger.error("Plugin failed to initialize", e);
          } catch (Exception e) {
            logger.error("Plugin failed to initialize", e);
          }
        }

      }
    } catch (IOException e) {
      // FIXME
    }
  }

  private void loadInternalPlugins() {
    Reflections reflections = new Reflections(
      RodaCoreFactory.getRodaConfigurationAsString("core", "plugins", "internal", "package"));
    Set<Class<? extends Plugin>> plugins = reflections.getSubTypesOf(Plugin.class);
    for (Class<? extends Plugin> plugin : plugins) {

      Plugin<?> p;
      try {
        p = (Plugin<?>) ClassLoaderUtility.createObject(plugin.getCanonicalName());
        p.init();
        internalPluginChache.put(plugin.getName(), p);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | PluginException e) {
        logger.error("Unable to instantiate plugin '{}'", plugin.getCanonicalName());
      }

    }
  }

  private Plugin<?> loadPlugin(Path jarFile, List<URL> jarURLs) {

    JarFile jar = null;
    Plugin<?> plugin = null;

    try {

      jar = new JarFile(jarFile.toFile());

      Manifest manifest = jar.getManifest();

      if (manifest == null) {
        logger.trace(jarFile.getFileName() + " doesn't have a MANIFEST file");
      } else {

        Attributes mainAttributes = manifest.getMainAttributes();

        String pluginClassName = mainAttributes.getValue(RODA_PLUGIN_MANIFEST_KEY);

        if (pluginClassName != null) {

          logger.trace(jarFile.getFileName() + " has plugin " + pluginClassName);

          logger.trace("Adding jar " + jarFile.getFileName() + " to classpath and loading " + pluginClassName
            + " with ClassLoader " + URLClassLoader.class.getSimpleName());

          Object object = ClassLoaderUtility.createObject(jarURLs.toArray(new URL[jarURLs.size()]), pluginClassName);

          if (Plugin.class.isAssignableFrom(object.getClass())) {

            plugin = (Plugin<?>) object;

          } else {
            logger.error(pluginClassName + " is not a valid Plugin");
          }

        } else {
          logger.trace(
            jarFile.getFileName() + " MANIFEST file doesn't have a '" + RODA_PLUGIN_MANIFEST_KEY + "' attribute");
        }

      }

    } catch (Throwable e) {

      logger.error(jarFile.getFileName() + " error loading plugin - " + e.getMessage(), e);

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

  protected class SearchPluginsTask extends TimerTask {

    public void run() {

      logger.debug("Searching for plugins...");

      loadPlugins();

      logger.debug("Search complete - " + jarPluginCache.size() + " jar files");

      for (Path jarFile : jarPluginCache.keySet()) {

        Plugin<?> plugin = jarPluginCache.get(jarFile).plugin;

        if (plugin != null) {
          logger.debug("- " + jarFile.getFileName());
          logger.debug("--- " + plugin.getName() + " - " + plugin.getVersion() + " - " + plugin.getDescription());
        }
      }
    }
  }

  protected class JarPlugin {

    private Plugin<?> plugin = null;
    private long lastModified = 0;

    JarPlugin(Plugin<?> plugin, long lastModified) {
      this.plugin = plugin;
      this.lastModified = lastModified;
    }
  }

  // FIXME delete this
  public static void main(String[] args) {
    Path rodaHomePath = Paths.get("/home/hsilva/.roda/");
    Path rodaConfigPath = rodaHomePath.resolve("config");
    Path rodaPluginsPath = rodaConfigPath.resolve("plugins");
    try {
      PluginManager defaultPluginManager = PluginManager.getDefaultPluginManager(rodaConfigPath, rodaPluginsPath);
      Thread.sleep(3 * 1000);
      Plugin<?> plugin = defaultPluginManager.getPlugin("org.roda.action.fixity.FixityAction");
      plugin.execute(null, null, null, null);
      Thread.sleep(60 * 1000);
    } catch (PluginManagerException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (PluginException e) {
      e.printStackTrace();
    }
  }

}
