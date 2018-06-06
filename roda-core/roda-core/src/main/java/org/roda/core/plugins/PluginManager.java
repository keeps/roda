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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
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
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.ClassLoaderUtility;
import org.roda.core.util.CompoundClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the RODA plugin manager. It is responsible for loading {@link Plugin}
 * s.
 * 
 * @author Rui Castro
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public class PluginManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

  private static final int LOAD_PLUGINS_MAX_CYCLES = 100;

  private static Path RODA_CONFIG_PATH = null;
  private static Path RODA_PLUGINS_PATH = null;
  private static Path RODA_PLUGINS_SHARED_PATH = null;
  private static String RODA_PLUGIN_MANIFEST_KEY = "RODA-Plugin";
  private static String RODA_PLUGIN_MANIFEST_KEY_DEPENDS = "RODA-Plugin-Depends";

  private Timer loadPluginsTimer = null;
  private Map<Path, JarPlugins> jarPluginCache = new HashMap<>();
  private Map<String, ClassLoader> jarPluginClassloaderCache = new HashMap<>();
  private Map<String, Plugin<? extends IsRODAObject>> internalPluginChache = new HashMap<>();
  private Map<String, Plugin<? extends IsRODAObject>> externalPluginChache = new HashMap<>();
  private Map<PluginType, List<PluginInfo>> pluginInfoPerType = new EnumMap<>(PluginType.class);
  private Map<String, Set<Class>> pluginObjectClasses = new HashMap<>();
  private Map<Class, List<PluginInfo>> pluginInfoPerObjectClass = new HashMap<>();
  private boolean internalPluginStarted = false;
  private List<String> blacklistedPlugins;

  /**
   * The default Plugin Manager instance.
   */
  private static PluginManager defaultPluginManager = null;

  /**
   * Constructs a new {@link PluginManager}.
   * 
   * @throws PluginManagerException
   */
  private PluginManager() throws PluginManagerException {
    // do nothing
  }

  /**
   * Gets the default {@link PluginManager}.
   * 
   * @return the default {@link PluginManager}.
   * 
   * @throws PluginManagerException
   */
  public static synchronized PluginManager instantiatePluginManager(Path rodaConfigPath, Path rodaPluginsPath)
    throws PluginManagerException {
    if (defaultPluginManager == null) {
      RODA_CONFIG_PATH = rodaConfigPath;
      RODA_PLUGINS_PATH = rodaPluginsPath;
      RODA_PLUGINS_SHARED_PATH = rodaPluginsPath.resolve(RodaConstants.CORE_PLUGINS_SHARED_FOLDER);
      defaultPluginManager = new PluginManager();
      defaultPluginManager.init();
    }
    return defaultPluginManager;
  }

  public static PluginManager getInstance() {
    return defaultPluginManager;
  }

  public <T extends IsRODAObject> void registerPlugin(Plugin<T> plugin) throws PluginException {
    try {
      plugin.init();
      externalPluginChache.put(plugin.getClass().getName(), plugin);
      processAndCachePluginInformation(plugin);
      LOGGER.debug("Plugin added dynamically started {} (version {})", plugin.getName(), plugin.getVersion());
    } catch (Throwable e) {
      // 20170123 hsilva: it is required to catch Throwable as there are some
      // linking errors that only will happen during the execution (e.g.
      // java.lang.NoSuchMethodError)
      throw new PluginException("An exception have occured during plugin registration", e);
    }
  }

  /**
   * Returns all {@link Plugin}s present in all jars.
   * 
   * @return a {@link List} of {@link Plugin}s.
   */
  public List<Plugin<? extends IsRODAObject>> getPlugins() {
    List<Plugin<? extends IsRODAObject>> plugins = new ArrayList<>();
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
    return getPlugins().stream().map(e -> getPluginInfo(e)).collect(Collectors.toList());
  }

  public List<PluginInfo> getPluginsInfo(PluginType pluginType) {
    return pluginInfoPerType.get(pluginType);
  }

  public List<PluginInfo> getPluginsInfo(List<PluginType> pluginTypes) {
    List<PluginInfo> pluginsInfo = new ArrayList<>();

    for (PluginType pluginType : pluginTypes) {
      pluginsInfo.addAll(pluginInfoPerType.getOrDefault(pluginType, Collections.emptyList()));
    }

    return pluginsInfo;
  }

  public Map<String, Set<Class>> getPluginObjectClasses() {
    return pluginObjectClasses;
  }

  public Set<Class> getPluginObjectClasses(String pluginID) {
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
   * Returns an instance of the {@link Plugin} with the specified ID (classname).
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

  private void init() {
    // load, for the first time, all the plugins (internal & external)
    loadPlugins();

    // schedule
    LOGGER.debug("Starting plugin scanner timer...");
    int timeInSeconds = RodaCoreFactory.getRodaConfiguration().getInt("core.plugins.external.scheduler.interval");
    this.loadPluginsTimer = new Timer("Plugin scanner timer", true);
    this.loadPluginsTimer.schedule(new SearchPluginsTask(), timeInSeconds * 1000, timeInSeconds * 1000);

    LOGGER.info("{} init OK", getClass().getSimpleName());
  }

  private <T extends IsRODAObject> PluginInfo getPluginInfo(Plugin<T> plugin) {
    return new PluginInfo(plugin.getClass().getName(), plugin.getName(), plugin.getVersion(), plugin.getDescription(),
      plugin.getType(), plugin.getCategories(), plugin.getParameters());
  }

  private void loadPlugins() {
    // reload backlisted plugins
    blacklistedPlugins = RodaCoreFactory.getRodaConfigurationAsList("core", "plugins", "blacklist");

    // load "external" RODA plugins, i.e., those available in the plugins folder
    if (FSUtils.exists(RODA_PLUGINS_PATH) && FSUtils.isDirectory(RODA_PLUGINS_PATH)) {
      loadExternalPlugins();
    }

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
            "'{}' has jars that will not be loaded as they are expected inside a folder (don't use folder '{}' to put them if you're not "
              + "100% sure that they should be used when loading each plugin. Instead, consider putting them inside folder '{}' to remove "
              + "this error)! And the jars are:",
            RODA_PLUGINS_PATH, RodaConstants.CORE_PLUGINS_SHARED_FOLDER, RodaConstants.CORE_PLUGINS_DISABLED_FOLDER);
          iterator.forEachRemaining(path -> LOGGER.error("   {}", path));
        }
      }

      // process each folder inside the plugins folder (except shared &
      // disabled)
      List<PluginLoadInfo> pluginLoadInfos = new ArrayList<>();

      try (DirectoryStream<Path> pluginsFolders = Files.newDirectoryStream(RODA_PLUGINS_PATH,
        path -> Files.isDirectory(path)
          && !RodaConstants.CORE_PLUGINS_SHARED_FOLDER.equals(path.getFileName().toString())
          && !RodaConstants.CORE_PLUGINS_DISABLED_FOLDER.equals(path.getFileName().toString()))) {
        for (Path pluginFolder : pluginsFolders) {
          pluginLoadInfos.addAll(loadExternalPluginFolder(sharedJarURLs, pluginFolder));
        }
      }

      for (int i = 0; i < LOAD_PLUGINS_MAX_CYCLES && !pluginLoadInfos.isEmpty(); i++) {
        LOGGER.debug("Running cycle for loading plugins & their dependencies (i = {})", i);

        // process plugins that do not have dependencies on other plugins
        pluginLoadInfos.stream().filter(p -> p.pluginDependencies.isEmpty()).forEach(p -> loadPlugin(p));
        pluginLoadInfos.removeIf(p -> p.pluginDependencies.isEmpty());

        // load plugins that have dependencies that are all loaded
        List<PluginLoadInfo> pluginWithDeps = pluginLoadInfos.stream().filter(p -> !p.pluginDependencies.isEmpty())
          .collect(Collectors.toList());
        for (PluginLoadInfo pluginLoadInfo : pluginWithDeps) {
          boolean allLoaded = true;
          for (String dependency : pluginLoadInfo.pluginDependencies) {
            if (!isPluginDependencyLoaded(dependency)) {
              allLoaded = false;
              break;
            }
          }

          if (allLoaded) {
            loadPlugin(pluginLoadInfo);
            pluginLoadInfos.remove(pluginLoadInfo);
          }
        }
      }

      if (!pluginLoadInfos.isEmpty()) {
        for (PluginLoadInfo pluginLoadInfo : pluginLoadInfos) {
          LOGGER.warn("Could not load the plugin {} due to dependencies not being loaded: {}", pluginLoadInfo.jarPath,
            pluginLoadInfo.pluginDependencies);
        }

        LOGGER.info("Loaded dependencies: {}", jarPluginClassloaderCache.keySet());

      }

    } catch (IOException e) {
      LOGGER.error("Error while instantiating external plugins", e);
    }
  }

  private class PluginLoadInfo {
    Path jarPath;
    JarFile jar;
    List<URL> jarClasspath;
    List<String> pluginClassNames;
    List<String> pluginDependencies;
    List<Path> pluginProperties;

    public PluginLoadInfo(Path jarPath, JarFile jar, List<URL> jarClasspath, List<String> pluginClassNames,
      List<String> pluginDepends, List<Path> pluginProperties) {
      super();
      this.jarPath = jarPath;
      this.jar = jar;
      this.jarClasspath = jarClasspath;
      this.pluginClassNames = pluginClassNames;
      this.pluginDependencies = pluginDepends;
      this.pluginProperties = pluginProperties;
    }

  }

  private List<PluginLoadInfo> loadExternalPluginFolder(List<URL> sharedJarURLs, Path pluginFolder) {
    LOGGER.debug("Processing plugin folder '{}'", pluginFolder);
    List<Path> pluginJarFiles = new ArrayList<>();
    List<URL> classpath = new ArrayList<>(sharedJarURLs);
    List<PluginLoadInfo> pluginLoadInfos = new ArrayList<>();
    List<Path> pluginProperties = new ArrayList<>();

    // add dependencies to classpath
    Path dependenciesFolder = pluginFolder.resolve(RodaConstants.CORE_PLUGINS_DEPENDENCIES_FOLDER);
    if (Files.exists(dependenciesFolder) && Files.isDirectory(dependenciesFolder)) {
      try (DirectoryStream<Path> jarsStream = Files.newDirectoryStream(dependenciesFolder, "*.jar")) {
        jarsStream.forEach(jarFile -> {
          try {
            classpath.add(jarFile.toUri().toURL());
          } catch (MalformedURLException e) {
            LOGGER.warn("Could not add jar to dependecies of plugin at {}", pluginFolder, e);
          }
        });
      } catch (IOException e) {
        LOGGER.warn("Could not load dependencies of plugin at {}", pluginFolder, e);
      }
    }

    // list jars to load
    try (DirectoryStream<Path> jarsStream = Files.newDirectoryStream(pluginFolder, "*.jar")) {
      jarsStream.forEach(jarFile -> pluginJarFiles.add(jarFile));
    } catch (NoSuchFileException e) {
      // do nothing as folder does not exist
    } catch (IOException e) {
      LOGGER.warn("Could not load jars of plugin at {}", pluginFolder, e);
    }

    // gather plugin configuration
    try (DirectoryStream<Path> propertiesStream = Files.newDirectoryStream(pluginFolder, "*.properties")) {
      propertiesStream.forEach(propertiesFile -> pluginProperties.add(propertiesFile));
    } catch (IOException e) {
      LOGGER.warn("Could not gather plugin properties at {}", pluginFolder, e);
    }

    for (Path jarFile : pluginJarFiles) {
      addJarToPluginToBeLoaded(jarFile, classpath, pluginLoadInfos, pluginProperties);
    }

    return pluginLoadInfos;

  }

  private void addJarToPluginToBeLoaded(Path jarPath, List<URL> classpath, List<PluginLoadInfo> pluginLoadInfos,
    List<Path> pluginProperties) {
    List<URL> jarClasspath = new ArrayList<>(classpath);

    try (JarFile jar = new JarFile(jarPath.toFile())) {
      // add own jar to classpath
      jarClasspath.add(jarPath.toUri().toURL());

      Manifest manifest = jar.getManifest();

      if (manifest == null) {
        LOGGER.trace("{} doesn't have a MANIFEST file", jarPath.getFileName());
      } else {

        Attributes mainAttributes = manifest.getMainAttributes();

        // Get plugin class names from manifest
        List<String> pluginClassNames = new ArrayList<>();
        String pluginClassNamesString = mainAttributes.getValue(RODA_PLUGIN_MANIFEST_KEY);
        if (pluginClassNamesString != null) {
          pluginClassNames.addAll(Arrays.asList(pluginClassNamesString.split("\\s+")));
        }

        // Get plugin names that this plugin depends on from manifest
        List<String> pluginDepends = new ArrayList<>();
        String pluginClassNamesDependsString = mainAttributes.getValue(RODA_PLUGIN_MANIFEST_KEY_DEPENDS);
        if (pluginClassNamesDependsString != null) {
          pluginDepends.addAll(Arrays.asList(pluginClassNamesDependsString.split("\\s+")));
        }

        pluginLoadInfos
          .add(new PluginLoadInfo(jarPath, jar, jarClasspath, pluginClassNames, pluginDepends, pluginProperties));

      }
    } catch (IOException e) {
      LOGGER.error("Error loading plugin from {}", jarPath.getFileName(), e);
    }
  }

  private boolean isPluginDependencyLoaded(String pluginDependencyRegex) {
    return getPluginClassLoader(pluginDependencyRegex) != null;
  }

  private ClassLoader getPluginClassLoader(String pluginDependencyRegex) {
    String foundIt = null;

    Pattern pattern = Pattern.compile(pluginDependencyRegex);
    for (String key : jarPluginClassloaderCache.keySet()) {
      if (pattern.matcher(key).matches()) {
        foundIt = key;
        break;
      }
    }

    return foundIt != null ? jarPluginClassloaderCache.get(foundIt) : null;

  }

  private String getPluginClassLoaderCacheKey(Path jarPath) {
    StringBuilder b = new StringBuilder();

    b.append(jarPath.getParent().getFileName().toString());
    b.append("/");
    b.append(jarPath.getFileName().toString());

    return b.toString();
  }

  private List<URL> getSharedJarURLs(Path folder) throws IOException {
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

  private void loadPlugin(PluginLoadInfo p) {
    BasicFileAttributes attrs;
    try {
      attrs = Files.readAttributes(p.jarPath, BasicFileAttributes.class);

      if (jarPluginCache.containsKey(p.jarPath)
        && attrs.lastModifiedTime().toMillis() == jarPluginCache.get(p.jarPath).lastModified) {
        LOGGER.debug("{} is already loaded", p.jarPath.getFileName());
      } else {
        // The Plugin doesn't exist or the modification date is
        // different.

        // Let's create Plugin classloader
        ClassLoader classloader;

        if (p.pluginDependencies.isEmpty()) {
          classloader = new URLClassLoader(p.jarClasspath.toArray(new URL[] {}), getClass().getClassLoader());
        } else {
          CompoundClassLoader c = new CompoundClassLoader();
          c.addLoader(new URLClassLoader(p.jarClasspath.toArray(new URL[] {}), getClass().getClassLoader()));

          p.pluginDependencies.forEach(d -> c.addLoader(getPluginClassLoader(d)));

          classloader = c;
        }

        // Let's load Plugin properties
        for (Path propertiesFile : p.pluginProperties) {
          try {
            RodaCoreFactory.addExternalConfiguration(propertiesFile);
          } catch (ConfigurationException e) {
            LOGGER.warn("Could not load plugin configuration: " + propertiesFile, e);
          }
        }

        // Let's load the Plugin
        List<Plugin<? extends IsRODAObject>> plugins = loadPlugin(p.jarPath, p.pluginClassNames, classloader);
        if (!plugins.isEmpty()) {
          LOGGER.info("'{}' (is new? {}) is not loaded or modification dates differ. Inspecting Jar...",
            p.jarPath.getFileName(), jarPluginCache.containsKey(p.jarPath));
        }
        for (Plugin<? extends IsRODAObject> plugin : plugins) {
          try {
            if (plugin != null && !blacklistedPlugins.contains(plugin.getClass().getName())) {
              plugin.init();
              externalPluginChache.put(plugin.getClass().getName(), plugin);
              processAndCachePluginInformation(plugin);
              LOGGER.info("Plugin started '{}' (version {})", plugin.getName(), plugin.getVersion());
            } else {
              LOGGER.trace("'{}' is not a Plugin", p.jarPath.getFileName());
            }

            synchronized (jarPluginCache) {
              if (jarPluginCache.get(p.jarPath) != null) {
                JarPlugins jarPlugins = jarPluginCache.get(p.jarPath);
                jarPlugins.plugins = new ArrayList<>();
                jarPlugins.plugins.add(plugin);
                jarPlugins.lastModified = attrs.lastModifiedTime().toMillis();
              } else {
                jarPluginCache.put(p.jarPath, new JarPlugins(plugin, attrs.lastModifiedTime().toMillis()));
              }
            }
          } catch (Exception | LinkageError e) {
            LOGGER.error("Plugin failed to initialize: {}", p.jarPath, e);
          }
        }

        // Let's cache Plugin classloader
        jarPluginClassloaderCache.put(getPluginClassLoaderCacheKey(p.jarPath), classloader);
      }
    } catch (IOException e1) {
      LOGGER.error("Plugin failed to initialize: {}", p.jarPath, e1);
    }
  }

  private void loadInternalPlugins() {
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

  private List<Plugin<?>> loadPlugin(Path jarPath, List<String> pluginClassNames, ClassLoader classloader) {
    List<Plugin<?>> ret = new ArrayList<>();
    Plugin<?> plugin = null;

    if (pluginClassNames != null) {
      for (String pluginClassName : pluginClassNames) {
        Object object;
        try {
          object = classloader.loadClass(pluginClassName).newInstance();

          if (Plugin.class.isAssignableFrom(object.getClass())) {
            plugin = (Plugin<?>) object;
            ret.add(plugin);
          } else {
            LOGGER.error("{} is not a valid Plugin", pluginClassNames);
          }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | RuntimeException e) {
          LOGGER.error("Error loading plugin from {}", jarPath, e);

        }
      }
    }
    return ret;
  }

  protected class SearchPluginsTask extends TimerTask {

    @Override
    public void run() {

      LOGGER.debug("Searching for plugins...");

      loadPlugins();

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Search complete - {} jar files", jarPluginCache.size());

        for (Entry<Path, JarPlugins> jarEntry : jarPluginCache.entrySet()) {
          Path jarFile = jarEntry.getKey();
          List<Plugin<?>> plugins = jarEntry.getValue().plugins;
          if (!plugins.isEmpty()) {
            for (Plugin<?> plugin : plugins) {
              LOGGER.debug("- {}", jarFile.getFileName());
              LOGGER.debug("--- {} - {} - {}", plugin.getName(), plugin.getVersion(), plugin.getDescription());
            }
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

  public static String getPluginsInformationAsMarkdown(List<String> plugins) {
    StringBuilder sb = new StringBuilder();
    int numberOfPlugins = plugins.size();
    if (numberOfPlugins > 1) {
      sb.append(String.format("# Plugins%n%n"));
    }
    for (String plugin : plugins) {
      try {
        Class<?> pluginClass = Class.forName(plugin);
        Plugin<? extends IsRODAObject> pluginInstance = (Plugin<? extends IsRODAObject>) pluginClass.newInstance();
        sb.append(getPluginInformationAsMarkdown(pluginInstance, numberOfPlugins > 1));
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        sb.append(String.format("#%s %s %n%n", numberOfPlugins > 1 ? "#" : "", plugin));
        sb.append(String.format("##%s Description %n%n%s%n%n", numberOfPlugins > 1 ? "#" : "",
          "Couldn't generate plugin info! Exception thrown: " + e.getClass().getSimpleName()));
      }
    }
    return sb.toString();
  }

  public static String getPluginInformationAsMarkdown(Plugin<? extends IsRODAObject> plugin) {
    return getPluginInformationAsMarkdown(plugin, false);
  }

  private static String getPluginInformationAsMarkdown(Plugin<? extends IsRODAObject> plugin, boolean severalPlugins) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("#%s %s %n%n", severalPlugins ? "#" : "", plugin.getName()));
    // 2018-01-24 hsilva: having the version usually depends on having the tool
    // installed, so lets not show that information
    sb.append(String.format("##%s Description %n%n%s%n%n", severalPlugins ? "#" : "", plugin.getDescription()));

    StringBuilder sbParameters = new StringBuilder();
    if (plugin.getParameters().isEmpty()) {
      sbParameters.append("No parameters.");
    } else {
      sbParameters.append(String.format("| **%s** | **%s** | **%s** | **%s** | **%s** | **%s** | %n", "Name",
        "Description", "Type", "Default value", "Possible values", "Mandatory", "Read only"));
      sbParameters.append(String.format("| --- | --- | --- | --- | --- | --- | --- | %n"));
      for (PluginParameter pluginParameter : plugin.getParameters()) {
        sbParameters.append(String.format("| **%s** | %s | %s | %s | %s | %s | %s | %n", pluginParameter.getName(),
          pluginParameter.getDescription().replaceAll("\n", ""), pluginParameter.getType(),
          pluginParameter.getDefaultValue(),
          pluginParameter.getPossibleValues().isEmpty() ? "" : pluginParameter.getPossibleValues(),
          pluginParameter.isMandatory(), pluginParameter.isReadonly()));
      }
    }
    sb.append(String.format("##%s Parameters %n%n%s%n%n", severalPlugins ? "#" : "", sbParameters.toString()));
    return sb.toString();
  }

}
