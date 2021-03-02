/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.protocols;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.ClassLoaderUtility;
import org.roda.core.util.CompoundClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the RODA protocol manager. It is responsible for loading
 * {@link Protocol} s.
 * 
 * @author Gabriel Barros <g.santos.barros@gmail.com>
 */
public class ProtocolManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

  private static final int LOAD_PROTOCOLS_MAX_CYCLES = 100;

  private static Path RODA_CONFIG_PATH = null;
  private static Path RODA_PROTOCOLS_PATH = null;
  private static Path RODA_PROTOCOLS_SHARED_PATH = null;
  private static String RODA_PROTOCOL_MANIFEST_KEY = "RODA-Protocol";
  private static String RODA_PROTOCOL_MANIFEST_KEY_DEPENDS = "RODA-Protocol-Depends";

  private Timer loadProtocolsTimer = null;
  private Map<Path, JarProtocols> jarProtocolCache = new HashMap<>();
  private Map<String, ClassLoader> jarProtocolClassloaderCache = new HashMap<>();
  private Map<String, Protocol> internalProtocolChache = new HashMap<>();
  private Map<String, Protocol> externalProtocolChache = new HashMap<>();
  private Map<String, Class> protocolObjectClasses = new HashMap<>();
  private boolean internalProtocolStarted = false;
  private List<String> blacklistedProtocols;

  /**
   * The default Protocol Manager instance.
   */
  private static ProtocolManager defaultProtocolManager = null;

  /**
   * Constructs a new {@link ProtocolManager}.
   * 
   * @throws ProtocolManagerException
   */
  private ProtocolManager() throws ProtocolManagerException {
    // do nothing
  }

  /**
   * Gets the default {@link ProtocolManager}.
   * 
   * @return the default {@link ProtocolManager}.
   * 
   * @throws ProtocolManagerException
   */
  public static synchronized ProtocolManager instantiateProtocolManager(Path rodaConfigPath, Path rodaProtocolsPath)
    throws ProtocolManagerException {
    if (defaultProtocolManager == null) {
      RODA_CONFIG_PATH = rodaConfigPath;
      RODA_PROTOCOLS_PATH = rodaProtocolsPath;
      RODA_PROTOCOLS_SHARED_PATH = rodaProtocolsPath.resolve(RodaConstants.CORE_PROTOCOLS_SHARED_FOLDER);
      defaultProtocolManager = new ProtocolManager();
      defaultProtocolManager.init();
    }
    return defaultProtocolManager;
  }

  public static ProtocolManager getInstance() {
    return defaultProtocolManager;
  }

  public <T extends IsRODAObject> void registerProtocol(Protocol protocol) throws ProtocolException {
    try {
      protocol.init();
      externalProtocolChache.put(protocol.getSchema(), protocol);
      processAndCacheProtocolInformation(protocol);
      LOGGER.debug("Protocol added dynamically started {} (version {})", protocol.getName(), protocol.getVersion());
    } catch (Throwable e) {
      throw new ProtocolException("An exception have occured during protocol registration", e);
    }
  }

  /**
   * Returns all {@link Protocol}s present in all jars.
   * 
   * @return a {@link List} of {@link Protocol}s.
   */
  public List<Protocol> getProtocols() {
    List<Protocol> protocols = new ArrayList<>();
    protocols.addAll(internalProtocolChache.values());
    protocols.addAll(externalProtocolChache.values());
    return protocols;
  }

  public Map<String, Class> getProtocolObjectClasses() {
    return protocolObjectClasses;
  }

  public Class getProtocolObjectClasses(String protocolID) {
    return protocolObjectClasses.get(protocolID);
  }

  public Class getProtocolObjectClasses(Protocol protocol) {
    return protocolObjectClasses.get(protocol.getClass().getName());
  }

  /**
   * Returns an instance of the {@link Protocol} with the specified URI
   * 
   * @param uri
   *          the Location of the resource.
   * 
   * @return a {@link Protocol} or <code>null</code> if the specified schema has
   *         not been registered as a protocol {@link Protocol} or something went
   *         wrong during its init().
   */
  public Protocol getProtocol(URI uri) throws ProtocolManagerException {
    Protocol protocol = null;

    Protocol cachedInternalProtocol = internalProtocolChache.get(uri.getScheme());
    if (cachedInternalProtocol != null) {
      protocol = cachedInternalProtocol.cloneMe(uri);
    }

    boolean internalProtocolTakesPrecedence = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.protocols.internal.take_precedence_over_external", true);
    Protocol cachedExternalProtocol = externalProtocolChache.get(uri.getScheme());
    if ((protocol == null || !internalProtocolTakesPrecedence) && cachedExternalProtocol != null) {
      protocol = cachedExternalProtocol.cloneMe(uri);
    }

    if (protocol == null) {
      throw new ProtocolManagerException("Cannot find any protocol handler for the schema " + uri.getScheme());
    }
    return protocol;
  }

  /**
   * This method should be called to stop {@link ProtocolManager} and all
   * {@link Protocol}s currently loaded.
   */
  public void shutdown() {

    if (this.loadProtocolsTimer != null) {
      // Stop the protocol loader timer
      this.loadProtocolsTimer.cancel();
    }

    for (JarProtocols jarProtocols : this.jarProtocolCache.values()) {
      for (Protocol protocol : jarProtocols.protocols) {
        if (protocol != null) {
          protocol.shutdown();
        }
      }
    }
  }

  private void init() {
    // load, for the first time, all the protocols (internal & external)
    loadProtocols();

    // schedule
    // TODO: this same behavior happens in plugins, is it really necessary?
    LOGGER.debug("Starting protocol scanner timer...");
    int timeInSeconds = RodaCoreFactory.getRodaConfiguration().getInt("core.protocols.external.scheduler.interval", 30);
    this.loadProtocolsTimer = new Timer("Protocol scanner timer", true);
    this.loadProtocolsTimer.schedule(new SearchProtocolsTask(), timeInSeconds * 1000, timeInSeconds * 1000);

    LOGGER.info("{} init OK", getClass().getSimpleName());
  }

  private void loadProtocols() {
    // reload backlisted protocols
    blacklistedProtocols = RodaCoreFactory.getRodaConfigurationAsList("core", "protocols", "blacklist");

    // load "external" RODA protocols, i.e., those available in the protocols folder
    if (FSUtils.exists(RODA_PROTOCOLS_PATH) && FSUtils.isDirectory(RODA_PROTOCOLS_PATH)) {
      loadExternalProtocols();
    }

    // load internal RODA protocols
    if (!internalProtocolStarted) {
      loadInternalProtocols();
    }
  }

  private void loadExternalProtocols() {
    try {
      // load shared jars
      List<URL> sharedJarURLs = getSharedJarURLs(RODA_PROTOCOLS_SHARED_PATH);

      // lets warn about jars that will not be loaded
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(RODA_PROTOCOLS_PATH, "*.jar")) {
        Iterator<Path> iterator = stream.iterator();
        if (iterator.hasNext()) {
          LOGGER.error(
            "'{}' has jars that will not be loaded as they are expected inside a folder (don't use folder '{}' to put them if you're not "
              + "100% sure that they should be used when loading each protocol. Instead, consider putting them inside folder '{}' to remove "
              + "this error)! And the jars are:",
            RODA_PROTOCOLS_PATH, RodaConstants.CORE_PROTOCOLS_SHARED_FOLDER,
            RodaConstants.CORE_PROTOCOLS_DISABLED_FOLDER);
          iterator.forEachRemaining(path -> LOGGER.error("   {}", path));
        }
      }

      // process each folder inside the protocols folder (except shared &
      // disabled)
      List<ProtocolLoadInfo> protocolLoadInfos = new ArrayList<>();

      try (DirectoryStream<Path> protocolsFolders = Files.newDirectoryStream(RODA_PROTOCOLS_PATH,
        path -> Files.isDirectory(path)
          && !RodaConstants.CORE_PROTOCOLS_SHARED_FOLDER.equals(path.getFileName().toString())
          && !RodaConstants.CORE_PROTOCOLS_DISABLED_FOLDER.equals(path.getFileName().toString()))) {
        for (Path protocolFolder : protocolsFolders) {
          protocolLoadInfos.addAll(loadExternalProtocolFolder(sharedJarURLs, protocolFolder));
        }
      }

      for (int i = 0; i < LOAD_PROTOCOLS_MAX_CYCLES && !protocolLoadInfos.isEmpty(); i++) {
        LOGGER.debug("Running cycle for loading protocols & their dependencies (i = {})", i);

        // process protocols that do not have dependencies on other protocols
        protocolLoadInfos.stream().filter(p -> p.protocolDependencies.isEmpty()).forEach(p -> loadProtocol(p));
        protocolLoadInfos.removeIf(p -> p.protocolDependencies.isEmpty());

        // load protocols that have dependencies that are all loaded
        List<ProtocolLoadInfo> protocolWithDeps = protocolLoadInfos.stream()
          .filter(p -> !p.protocolDependencies.isEmpty()).collect(Collectors.toList());
        for (ProtocolLoadInfo protocolLoadInfo : protocolWithDeps) {
          boolean allLoaded = true;
          for (String dependency : protocolLoadInfo.protocolDependencies) {
            if (!isProtocolDependencyLoaded(dependency)) {
              allLoaded = false;
              break;
            }
          }

          if (allLoaded) {
            loadProtocol(protocolLoadInfo);
            protocolLoadInfos.remove(protocolLoadInfo);
          }
        }
      }

      if (!protocolLoadInfos.isEmpty()) {
        for (ProtocolLoadInfo protocolLoadInfo : protocolLoadInfos) {
          LOGGER.warn("Could not load the protocol {} due to dependencies not being loaded: {}",
            protocolLoadInfo.jarPath, protocolLoadInfo.protocolDependencies);
        }

        LOGGER.info("Loaded dependencies: {}", jarProtocolClassloaderCache.keySet());

      }

    } catch (IOException e) {
      LOGGER.error("Error while instantiating external protocols", e);
    }
  }

  private class ProtocolLoadInfo {
    Path jarPath;
    JarFile jar;
    List<URL> jarClasspath;
    List<String> protocolClassNames;
    List<String> protocolDependencies;
    List<Path> protocolProperties;

    public ProtocolLoadInfo(Path jarPath, JarFile jar, List<URL> jarClasspath, List<String> protocolClassNames,
      List<String> protocolDepends, List<Path> protocolProperties) {
      super();
      this.jarPath = jarPath;
      this.jar = jar;
      this.jarClasspath = jarClasspath;
      this.protocolClassNames = protocolClassNames;
      this.protocolDependencies = protocolDepends;
      this.protocolProperties = protocolProperties;
    }

  }

  private List<ProtocolLoadInfo> loadExternalProtocolFolder(List<URL> sharedJarURLs, Path protocolFolder) {
    LOGGER.debug("Processing protocol folder '{}'", protocolFolder);
    List<Path> protocolJarFiles = new ArrayList<>();
    List<URL> classpath = new ArrayList<>(sharedJarURLs);
    List<ProtocolLoadInfo> protocolLoadInfos = new ArrayList<>();
    List<Path> protocolProperties = new ArrayList<>();

    // add dependencies to classpath
    Path dependenciesFolder = protocolFolder.resolve(RodaConstants.CORE_PROTOCOLS_DEPENDENCIES_FOLDER);
    if (Files.exists(dependenciesFolder) && Files.isDirectory(dependenciesFolder)) {
      try (DirectoryStream<Path> jarsStream = Files.newDirectoryStream(dependenciesFolder, "*.jar")) {
        jarsStream.forEach(jarFile -> {
          try {
            classpath.add(jarFile.toUri().toURL());
          } catch (MalformedURLException e) {
            LOGGER.warn("Could not add jar to dependecies of protocol at {}", protocolFolder, e);
          }
        });
      } catch (IOException e) {
        LOGGER.warn("Could not load dependencies of protocol at {}", protocolFolder, e);
      }
    }

    // list jars to load
    try (DirectoryStream<Path> jarsStream = Files.newDirectoryStream(protocolFolder, "*.jar")) {
      jarsStream.forEach(jarFile -> protocolJarFiles.add(jarFile));
    } catch (NoSuchFileException e) {
      // do nothing as folder does not exist
    } catch (IOException e) {
      LOGGER.warn("Could not load jars of protocol at {}", protocolFolder, e);
    }

    // gather protocol configuration
    try (DirectoryStream<Path> propertiesStream = Files.newDirectoryStream(protocolFolder, "*.properties")) {
      propertiesStream.forEach(propertiesFile -> protocolProperties.add(propertiesFile));
    } catch (IOException e) {
      LOGGER.warn("Could not gather protocol properties at {}", protocolFolder, e);
    }

    for (Path jarFile : protocolJarFiles) {
      addJarToProtocolToBeLoaded(jarFile, classpath, protocolLoadInfos, protocolProperties);
    }

    return protocolLoadInfos;

  }

  private void addJarToProtocolToBeLoaded(Path jarPath, List<URL> classpath, List<ProtocolLoadInfo> protocolLoadInfos,
    List<Path> protocolProperties) {
    List<URL> jarClasspath = new ArrayList<>(classpath);

    try (JarFile jar = new JarFile(jarPath.toFile())) {
      // add own jar to classpath
      jarClasspath.add(jarPath.toUri().toURL());

      Manifest manifest = jar.getManifest();

      if (manifest == null) {
        LOGGER.trace("{} doesn't have a MANIFEST file", jarPath.getFileName());
      } else {

        Attributes mainAttributes = manifest.getMainAttributes();

        // Get protocol class names from manifest
        List<String> protocolClassNames = new ArrayList<>();
        String protocolClassNamesString = mainAttributes.getValue(RODA_PROTOCOL_MANIFEST_KEY);
        if (protocolClassNamesString != null) {
          protocolClassNames.addAll(Arrays.asList(protocolClassNamesString.split("\\s+")));
        }

        // Get protocol names that this protocol depends on from manifest
        List<String> protocolDepends = new ArrayList<>();
        String protocolClassNamesDependsString = mainAttributes.getValue(RODA_PROTOCOL_MANIFEST_KEY_DEPENDS);
        if (protocolClassNamesDependsString != null) {
          protocolDepends.addAll(Arrays.asList(protocolClassNamesDependsString.split("\\s+")));
        }

        protocolLoadInfos.add(
          new ProtocolLoadInfo(jarPath, jar, jarClasspath, protocolClassNames, protocolDepends, protocolProperties));

      }
    } catch (IOException e) {
      LOGGER.error("Error loading protocol from {}", jarPath.getFileName(), e);
    }
  }

  private boolean isProtocolDependencyLoaded(String protocolDependencyRegex) {
    return getProtocolClassLoader(protocolDependencyRegex) != null;
  }

  private ClassLoader getProtocolClassLoader(String protocolDependencyRegex) {
    String foundIt = null;

    Pattern pattern = Pattern.compile(protocolDependencyRegex);
    for (String key : jarProtocolClassloaderCache.keySet()) {
      if (pattern.matcher(key).matches()) {
        foundIt = key;
        break;
      }
    }

    return foundIt != null ? jarProtocolClassloaderCache.get(foundIt) : null;

  }

  private String getProtocolClassLoaderCacheKey(Path jarPath) {
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

  private void loadProtocol(ProtocolLoadInfo p) {
    BasicFileAttributes attrs;
    try {
      attrs = Files.readAttributes(p.jarPath, BasicFileAttributes.class);

      if (jarProtocolCache.containsKey(p.jarPath)
        && attrs.lastModifiedTime().toMillis() == jarProtocolCache.get(p.jarPath).lastModified) {
        LOGGER.debug("{} is already loaded", p.jarPath.getFileName());
      } else {
        // The Protocol doesn't exist or the modification date is
        // different.

        // Let's create Protocol classloader
        ClassLoader classloader;

        if (p.protocolDependencies.isEmpty()) {
          classloader = new URLClassLoader(p.jarClasspath.toArray(new URL[] {}), getClass().getClassLoader());
        } else {
          CompoundClassLoader c = new CompoundClassLoader();
          c.addLoader(new URLClassLoader(p.jarClasspath.toArray(new URL[] {}), getClass().getClassLoader()));

          p.protocolDependencies.forEach(d -> c.addLoader(getProtocolClassLoader(d)));

          classloader = c;
        }

        // Let's load Protocol properties
        for (Path propertiesFile : p.protocolProperties) {
          try {
            RodaCoreFactory.addExternalConfiguration(propertiesFile);
          } catch (ConfigurationException e) {
            LOGGER.warn("Could not load protocol configuration: " + propertiesFile, e);
          }
        }

        // Let's load the Protocol
        List<Protocol> protocols = loadProtocol(p.jarPath, p.protocolClassNames, classloader);
        if (!protocols.isEmpty()) {
          LOGGER.info("'{}' (is new? {}) is not loaded or modification dates differ. Inspecting Jar...",
            p.jarPath.getFileName(), jarProtocolCache.containsKey(p.jarPath));
        }
        for (Protocol protocol : protocols) {
          try {
            if (protocol != null && !blacklistedProtocols.contains(protocol.getClass().getName())) {
              protocol.init();
              externalProtocolChache.put(protocol.getSchema(), protocol);
              processAndCacheProtocolInformation(protocol);
              LOGGER.info("Protocol started '{}' (version {})", protocol.getName(), protocol.getVersion());
            } else {
              LOGGER.trace("'{}' is not a Protocol", p.jarPath.getFileName());
            }

            synchronized (jarProtocolCache) {
              if (jarProtocolCache.get(p.jarPath) != null) {
                JarProtocols jarProtocols = jarProtocolCache.get(p.jarPath);
                jarProtocols.protocols = new ArrayList<>();
                jarProtocols.protocols.add(protocol);
                jarProtocols.lastModified = attrs.lastModifiedTime().toMillis();
              } else {
                jarProtocolCache.put(p.jarPath, new JarProtocols(protocol, attrs.lastModifiedTime().toMillis()));
              }
            }
          } catch (Exception | LinkageError e) {
            LOGGER.error("Protocol failed to initialize: {}", p.jarPath, e);
          }
        }

        // Let's cache Protocol classloader
        jarProtocolClassloaderCache.put(getProtocolClassLoaderCacheKey(p.jarPath), classloader);
      }
    } catch (IOException e1) {
      LOGGER.error("Protocol failed to initialize: {}", p.jarPath, e1);
    }
  }

  private void loadInternalProtocols() {
    Reflections reflections = new Reflections(
      RodaCoreFactory.getRodaConfigurationAsString("core", "protocols", "internal", "package"));
    Set<Class<? extends AbstractProtocol>> protocols = reflections.getSubTypesOf(AbstractProtocol.class);

    for (Class<? extends AbstractProtocol> protocol : protocols) {
      String name = protocol.getName();
      if (!Modifier.isAbstract(protocol.getModifiers()) && !blacklistedProtocols.contains(name)) {
        LOGGER.debug("Loading internal protocol '{}'", name);
        try {
          Protocol p = (Protocol) ClassLoaderUtility.createObject(protocol.getName());
          p.init();
          internalProtocolChache.put(p.getSchema(), p);
          processAndCacheProtocolInformation(p);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ProtocolException
          | RuntimeException e) {
          LOGGER.error("Unable to instantiate protocol '{}'", protocol.getName(), e);
        }
      }
    }
    internalProtocolStarted = true;
  }

  private <T extends IsRODAObject> void processAndCacheProtocolInformation(Protocol protocol) {

    // cache protocol > objectClasses
    protocolObjectClasses.put(protocol.getClass().getName(), protocol.getClass());

  }

  private List<Protocol> loadProtocol(Path jarPath, List<String> protocolClassNames, ClassLoader classloader) {
    List<Protocol> ret = new ArrayList<>();
    Protocol protocol = null;

    if (protocolClassNames != null) {
      for (String protocolClassName : protocolClassNames) {
        Object object;
        try {
          object = classloader.loadClass(protocolClassName).newInstance();

          if (Protocol.class.isAssignableFrom(object.getClass())) {
            protocol = (Protocol) object;
            ret.add(protocol);
          } else {
            LOGGER.error("{} is not a valid Protocol", protocolClassNames);
          }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | RuntimeException e) {
          LOGGER.error("Error loading protocol from {}", jarPath, e);

        }
      }
    }
    return ret;
  }

  protected class SearchProtocolsTask extends TimerTask {

    @Override
    public void run() {

      LOGGER.debug("Searching for protocols...");

      loadProtocols();

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Search complete - {} jar files", jarProtocolCache.size());

        for (Entry<Path, JarProtocols> jarEntry : jarProtocolCache.entrySet()) {
          Path jarFile = jarEntry.getKey();
          List<Protocol> protocols = jarEntry.getValue().protocols;
          if (!protocols.isEmpty()) {
            for (Protocol protocol : protocols) {
              LOGGER.debug("- {}", jarFile.getFileName());
              LOGGER.debug("--- {} - {} - {}", protocol.getName(), protocol.getVersion(), protocol.getDescription());
            }
          }
        }
      }
    }
  }

  protected class JarProtocols {
    protected List<Protocol> protocols = new ArrayList<>();
    private long lastModified = 0;

    JarProtocols(Protocol protocol, long lastModified) {
      protocols.add(protocol);
      this.lastModified = lastModified;
    }
  }

}
