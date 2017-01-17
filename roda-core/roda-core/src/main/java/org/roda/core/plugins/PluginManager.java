/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.util.ClassLoaderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the RODA plugin manager. It is responsible for loading {@link Plugin}
 * s.
 * 
 * @author Rui Castro
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class PluginManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

  private static Path RODA_CONFIG_PATH = null;
  private static Path RODA_PLUGINS_PATH = null;
  private static Path RODA_PLUGINS_SHARED_PATH = null;

  private static String RODA_PLUGIN_MANIFEST_KEY = "RODA-Plugin";

  private Timer loadPluginsTimer = null;
  private Map<Path, JarPlugins> jarPluginCache = new HashMap<Path, JarPlugins>();
  private Map<String, Plugin<? extends IsRODAObject>> internalPluginChache = new HashMap<String, Plugin<?>>();
  private Map<String, Plugin<? extends IsRODAObject>> externalPluginChache = new HashMap<String, Plugin<?>>();
  private Map<PluginType, List<PluginInfo>> pluginInfoPerType = new HashMap<PluginType, List<PluginInfo>>();
  private Map<String, Set<Class>> pluginObjectClasses = new HashMap<String, Set<Class>>();
  private Map<Class, List<PluginInfo>> pluginInfoPerObjectClass = new HashMap<Class, List<PluginInfo>>();
  private boolean internalPluginStarted = false;
  private List<String> blacklistedPlugins;

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
      RODA_PLUGINS_SHARED_PATH = rodaPluginsPath.resolve(RodaConstants.CORE_PLUGINS_SHARED_FOLDER);
      defaultPluginManager = new PluginManager();
    }
    return defaultPluginManager;
  }

  public <T extends IsRODAObject> void registerPlugin(Plugin<T> plugin) throws PluginException {
    try {
      plugin.init();
      externalPluginChache.put(plugin.getClass().getName(), plugin);
      processAndCachePluginInformation(plugin);
      LOGGER.debug("Plugin added dynamically started {} (version {})", plugin.getName(), plugin.getVersion());
    } catch (Exception e) {
      throw new PluginException("An exception have occured during plugin registration", e);
    }
  }

  /**
   * Returns all {@link Plugin}s present in all jars.
   * 
   * @return a {@link List} of {@link Plugin}s.
   */
  public List<Plugin<? extends IsRODAObject>> getPlugins() {
    List<Plugin<? extends IsRODAObject>> plugins = new ArrayList<Plugin<?>>();

    plugins.addAll(internalPluginChache.values());

    plugins.addAll(externalPluginChache.values());

    return plugins;
  }

  /**
   * Returns the {@link PluginInfo}s for all {@link Plugin}s.
   * 
   * @return a {@link List} of {@link PluginInfo}s.
   */
  public List<PluginInfo> getPluginsInfo() {
    List<PluginInfo> pluginsInfo = new ArrayList<PluginInfo>();

    for (Plugin<? extends IsRODAObject> plugin : getPlugins()) {
      pluginsInfo.add(getPluginInfo(plugin));
    }

    return pluginsInfo;
  }

  public List<PluginInfo> getPluginsInfo(PluginType pluginType) {
    return pluginInfoPerType.get(pluginType);
  }

  public List<PluginInfo> getPluginsInfo(List<PluginType> pluginTypes) {
    List<PluginInfo> pluginsInfo = new ArrayList<PluginInfo>();

    for (PluginType pluginType : pluginTypes) {
      pluginsInfo.addAll(pluginInfoPerType.getOrDefault(pluginType, Collections.emptyList()));
    }

    return pluginsInfo;
  }

  public Map<String, Set<Class>> getPluginObjectClasses() {
    return pluginObjectClasses;
  }

  public <T extends IsRODAObject> Set<Class> getPluginObjectClasses(String pluginID) {
    return pluginObjectClasses.get(pluginID);
  }

  public <T extends IsRODAObject> Set<Class> getPluginObjectClasses(Plugin<T> plugin) {
    return pluginObjectClasses.get(plugin.getClass().getName());
  }

  public Map<Class, List<PluginInfo>> getPluginInfoPerObjectClass() {
    return pluginInfoPerObjectClass;
  }

  public List<PluginInfo> getPluginInfoPerObjectClass(Class clazz) {
    return pluginInfoPerObjectClass.get(clazz);
  }

  public List<PluginInfo> getPluginInfoPerObjectClass(String className) {
    try {
      return pluginInfoPerObjectClass.get(Class.forName(className));
    } catch (ClassNotFoundException e) {
      return new ArrayList<>();
    }
  }

  /**
   * Returns an instance of the {@link Plugin} with the specified ID
   * (classname).
   * 
   * @param pluginID
   *          the ID (classname) of the {@link Plugin}.
   * 
   * @return a {@link Plugin} or <code>null</code> if the specified classname is
   *         not a {@link Plugin} or something went wrong during its init().
   */
  public Plugin<? extends IsRODAObject> getPlugin(String pluginID) {
    Plugin<? extends IsRODAObject> plugin = null;

    Plugin<? extends IsRODAObject> cachedInternalPlugin = internalPluginChache.get(pluginID);
    if (cachedInternalPlugin != null) {
      plugin = cachedInternalPlugin.cloneMe();
    }

    boolean internalPluginTakesPrecedence = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.internal.take_precedence_over_external");
    Plugin<? extends IsRODAObject> cachedExternalPlugin = externalPluginChache.get(pluginID);
    if ((plugin == null || !internalPluginTakesPrecedence) && cachedExternalPlugin != null) {
      plugin = cachedExternalPlugin.cloneMe();
    }

    return plugin;
  }

  public <T extends IsRODAObject> Plugin<T> getPlugin(String pluginID, Class<T> pluginClass) {
    return (Plugin<T>) getPlugin(pluginID);
  }

  /**
   * @param pluginID
   * 
   * @return {@link PluginInfo} or <code>null</code>.
   */
  public PluginInfo getPluginInfo(String pluginID) {
    Plugin<? extends IsRODAObject> plugin = getPlugin(pluginID);
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

    for (JarPlugins jarPlugins : this.jarPluginCache.values()) {
      for (Plugin<? extends IsRODAObject> plugin : jarPlugins.plugins) {
        if (plugin != null) {
          plugin.shutdown();
        }
      }
    }
  }

  /**
   * Constructs a new {@link PluginManager}.
   * 
   * @throws PluginManagerException
   */
  private PluginManager() throws PluginManagerException {
    LOGGER.debug("Starting plugin scanner timer...");

    int timeInSeconds = RodaCoreFactory.getRodaConfiguration().getInt("core.plugins.external.scheduler.interval");
    this.loadPluginsTimer = new Timer("Plugin scanner timer", true);
    this.loadPluginsTimer.schedule(new SearchPluginsTask(), 1000, timeInSeconds * 1000);

    LOGGER.info("{} init OK", getClass().getSimpleName());
  }

  private <T extends IsRODAObject> PluginInfo getPluginInfo(Plugin<T> plugin) {
    return new PluginInfo(plugin.getClass().getName(), plugin.getName(), plugin.getVersion(), plugin.getDescription(),
      plugin.getType(), plugin.getCategories(), plugin.getParameters());
  }

  private void loadPlugins() {
    // load "external" RODA plugins, i.e., those available in the plugins folder
    loadExternalPlugins();

    // load internal RODA plugins
    if (!internalPluginStarted) {
      loadInternalPlugins();
    }
  }

  private void loadExternalPlugins() {
    try {
      // load shared jars
      List<URL> sharedJarURLs = getSharedJarURLs(RODA_PLUGINS_SHARED_PATH);

      // lets warn about jars that will not be loaded
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(RODA_PLUGINS_PATH, "*.jar")) {
        Iterator<Path> iterator = stream.iterator();
        if (iterator.hasNext()) {
          LOGGER.error(
            "'{}' has jars that will not be loaded as they are expected inside a folder (don't use folder '{}' to put them if you're not 100% sure that they should be used when loading each plugin. Instead, consider putting them inside folder '{}' to remove this error)! And the jars are:",
            RODA_PLUGINS_PATH, RodaConstants.CORE_PLUGINS_SHARED_FOLDER, RodaConstants.CORE_PLUGINS_DEACTIVATED_FOLDER);
          iterator.forEachRemaining(path -> LOGGER.error("   {}", path));
        }
      }

      // process each folder inside the plugins folder (except shared & deactivated)
      try (DirectoryStream<Path> pluginsFolders = Files.newDirectoryStream(RODA_PLUGINS_PATH,
        path -> Files.isDirectory(path)
          && !RodaConstants.CORE_PLUGINS_SHARED_FOLDER.equals(path.getFileName().toString())
          && !RodaConstants.CORE_PLUGINS_DEACTIVATED_FOLDER.equals(path.getFileName().toString()))) {
        for (Path pluginFolder : pluginsFolders) {
          LOGGER.debug("Processing plugin folder '{}'", pluginFolder);
          List<Path> pluginJarFiles = new ArrayList<>();
          List<URL> pluginJarURLs = new ArrayList<>();
          try (DirectoryStream<Path> jarsStream = Files.newDirectoryStream(pluginFolder, "*.jar")) {
            for (Path jarFile : jarsStream) {
              LOGGER.debug("   Will process jar '{}'", jarFile);
              pluginJarFiles.add(jarFile);
              pluginJarURLs.add(jarFile.toUri().toURL());
            }
          } catch (NoSuchFileException e) {
            // do nothing as folder does not exist
          }

          pluginJarURLs.addAll(sharedJarURLs);
          URL[] jars = pluginJarURLs.toArray(new URL[pluginJarURLs.size()]);
          for (Path jarFile : pluginJarFiles) {
            processJar(jarFile, jars);
          }
        }
      }

    } catch (IOException e) {
      LOGGER.error("Error while instantiating external plugins", e);
    }
  }

  private List<URL> getSharedJarURLs(Path folder) throws MalformedURLException, IOException {
    List<URL> sharedJarURLs = new ArrayList<>();
    try (DirectoryStream<Path> sharedStream = Files.newDirectoryStream(folder, "*.jar")) {
      for (Path jarFile : sharedStream) {
        sharedJarURLs.add(jarFile.toUri().toURL());
      }
    } catch (NoSuchFileException e) {
      // do nothing as folder does not exist
    }
    return sharedJarURLs;
  }

  private void processJar(Path jarFile, URL[] jars) throws IOException {
    BasicFileAttributes attrs = Files.readAttributes(jarFile, BasicFileAttributes.class);

    if (jarPluginCache.containsKey(jarFile)
      && attrs.lastModifiedTime().toMillis() == jarPluginCache.get(jarFile).lastModified) {
      // The plugin already exists

      LOGGER.debug("{} is already loaded", jarFile.getFileName());

    } else {
      // The plugin doesn't exist or the modification date is
      // different. Let's load the Plugin

      LOGGER.debug("{} is not loaded or modification dates differ. Inspecting Jar...", jarFile.getFileName());

      List<Plugin<? extends IsRODAObject>> plugins = loadPlugin(jarFile, jars);

      for (Plugin<? extends IsRODAObject> plugin : plugins) {
        try {
          if (plugin != null && !blacklistedPlugins.contains(plugin.getClass().getName())) {

            plugin.init();
            externalPluginChache.put(plugin.getClass().getName(), plugin);
            processAndCachePluginInformation(plugin);
            LOGGER.debug("Plugin started {} (version {})", plugin.getName(), plugin.getVersion());

          } else {

            LOGGER.trace("{} is not a Plugin", jarFile.getFileName());

          }

          synchronized (jarPluginCache) {
            if (jarPluginCache.get(jarFile) != null) {
              jarPluginCache.get(jarFile).plugins.add(plugin);
            } else {
              jarPluginCache.put(jarFile, new JarPlugins(plugin,
                Files.readAttributes(jarFile, BasicFileAttributes.class).lastModifiedTime().toMillis()));
            }
          }
        } catch (Exception | LinkageError e) {
          LOGGER.error("Plugin failed to initialize: {}", jarFile, e);
        }
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private <T extends IsRODAObject> void loadInternalPlugins() {
    Reflections reflections = new Reflections(
      RodaCoreFactory.getRodaConfigurationAsString("core", "plugins", "internal", "package"));
    Set<Class<? extends AbstractPlugin>> plugins = reflections.getSubTypesOf(AbstractPlugin.class);
    plugins.addAll(reflections.getSubTypesOf(AbstractAIPComponentsPlugin.class));

    for (Class<? extends AbstractPlugin> plugin : plugins) {
      String name = plugin.getName();
      if (!Modifier.isAbstract(plugin.getModifiers()) && !blacklistedPlugins.contains(name)) {
        LOGGER.debug("Loading internal plugin '{}'", name);
        try {
          Plugin<? extends IsRODAObject> p = (Plugin<?>) ClassLoaderUtility.createObject(plugin.getName());
          p.init();
          internalPluginChache.put(plugin.getName(), p);
          processAndCachePluginInformation(p);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | PluginException
          | RuntimeException e) {
          LOGGER.error("Unable to instantiate plugin '{}'", plugin.getName(), e);
        }
      }
    }
    internalPluginStarted = true;
  }

  private <T extends IsRODAObject> void processAndCachePluginInformation(Plugin<T> plugin) {

    // cache plugin > objectClasses
    Set<Class> objectClasses = new HashSet<>(plugin.getObjectClasses());
    if (objectClasses.contains(AIP.class)) {
      objectClasses.add(IndexedAIP.class);
    } else if (objectClasses.contains(IndexedAIP.class)) {
      objectClasses.add(AIP.class);
    }
    if (objectClasses.contains(Representation.class)) {
      objectClasses.add(IndexedRepresentation.class);
    } else if (objectClasses.contains(IndexedRepresentation.class)) {
      objectClasses.add(Representation.class);
    }
    if (objectClasses.contains(File.class)) {
      objectClasses.add(IndexedFile.class);
    } else if (objectClasses.contains(IndexedFile.class)) {
      objectClasses.add(File.class);
    }
    if (objectClasses.contains(Risk.class)) {
      objectClasses.add(IndexedRisk.class);
    } else if (objectClasses.contains(IndexedRisk.class)) {
      objectClasses.add(Risk.class);
    }
    if (objectClasses.contains(DIP.class)) {
      objectClasses.add(IndexedDIP.class);
    } else if (objectClasses.contains(IndexedDIP.class)) {
      objectClasses.add(DIP.class);
    }
    if (objectClasses.contains(Report.class)) {
      objectClasses.add(IndexedReport.class);
    } else if (objectClasses.contains(IndexedReport.class)) {
      objectClasses.add(Report.class);
    }
    pluginObjectClasses.put(plugin.getClass().getName(), objectClasses);

    // cache plugintype > plugininfos
    PluginInfo pluginInfo = getPluginInfo(plugin.getClass().getName());
    objectClasses.stream().forEach(objectClass -> pluginInfo.addObjectClass(objectClass.getName()));
    PluginType pluginType = plugin.getType();
    if (pluginInfoPerType.get(pluginType) == null) {
      List<PluginInfo> list = new ArrayList<>();
      list.add(pluginInfo);
      pluginInfoPerType.put(pluginType, list);
    } else if (!pluginInfoPerType.get(pluginType).contains(pluginInfo)) {
      pluginInfoPerType.get(pluginType).add(pluginInfo);
    }

    // cache objectClass > plugininfos
    for (Class class1 : getPluginObjectClasses(plugin)) {
      if (pluginInfoPerObjectClass.get(class1) == null) {
        List<PluginInfo> list = new ArrayList<>();
        list.add(pluginInfo);
        pluginInfoPerObjectClass.put(class1, list);
      } else {
        pluginInfoPerObjectClass.get(class1).add(pluginInfo);
      }
    }

  }

  private List<Plugin<?>> loadPlugin(Path jarFile, URL[] jars) {

    JarFile jar = null;
    List<Plugin<?>> ret = new ArrayList<>();
    Plugin<?> plugin = null;

    try {
      jar = new JarFile(jarFile.toFile());

      Manifest manifest = jar.getManifest();

      if (manifest == null) {
        LOGGER.trace("{} doesn't have a MANIFEST file", jarFile.getFileName());
      } else {

        Attributes mainAttributes = manifest.getMainAttributes();

        String pluginClassNames = mainAttributes.getValue(RODA_PLUGIN_MANIFEST_KEY);

        if (pluginClassNames != null) {
          for (String pluginClassName : pluginClassNames.split("\\s+")) {

            LOGGER.trace("{} has plugin {}", jarFile.getFileName(), pluginClassName);
            LOGGER.trace("Adding jar {} to classpath and loading {} with ClassLoader {}", jarFile.getFileName(),
              pluginClassName, URLClassLoader.class.getSimpleName());

            Object object = ClassLoaderUtility.createObject(jars, pluginClassName);

            if (Plugin.class.isAssignableFrom(object.getClass())) {

              plugin = (Plugin<?>) object;
              ret.add(plugin);

            } else {
              LOGGER.error("{} is not a valid Plugin", pluginClassNames);
            }
          }

        } else {
          LOGGER.trace("{} MANIFEST file doesn't have a '{}' attribute", jarFile.getFileName(),
            RODA_PLUGIN_MANIFEST_KEY);
        }

      }

    } catch (IOException | ClassNotFoundException | NoClassDefFoundError | InstantiationException
      | IllegalAccessException | RuntimeException e) {

      LOGGER.error("Error loading plugin from {}", jarFile.getFileName(), e);

    } finally {

      if (jar != null) {
        try {
          jar.close();
        } catch (IOException e) {
          LOGGER.debug("Error closing jar {}", jarFile.getFileName(), e);
        }
      }
    }

    return ret;
  }

  protected class SearchPluginsTask extends TimerTask {

    public void run() {

      LOGGER.debug("Searching for plugins...");

      blacklistedPlugins = RodaCoreFactory.getRodaConfigurationAsList("core", "plugins", "blacklist");

      loadPlugins();

      LOGGER.debug("Search complete - {} jar files", jarPluginCache.size());

      for (Path jarFile : jarPluginCache.keySet()) {

        List<Plugin<?>> plugins = jarPluginCache.get(jarFile).plugins;

        if (!plugins.isEmpty()) {
          for (Plugin<?> plugin : plugins) {
            LOGGER.debug("- {}", jarFile.getFileName());
            LOGGER.debug("--- {} - {} - {}", plugin.getName(), plugin.getVersion(), plugin.getDescription());
          }
        }
      }
    }
  }

  protected class JarPlugins {

    protected List<Plugin<?>> plugins = new ArrayList<>();
    private long lastModified = 0;

    JarPlugins(Plugin<?> plugin, long lastModified) {
      plugins.add(plugin);
      this.lastModified = lastModified;
    }
  }

}
