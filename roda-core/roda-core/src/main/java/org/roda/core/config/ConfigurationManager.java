package org.roda.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.MergeCombiner;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaPropertiesReloadStrategy;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class ConfigurationManager {
  private final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
  private static ConfigurationManager instance;
  private boolean instantiated = false;
  private boolean instantiatedWithoutErrors = true;
  // Configuration related objects
  private CompositeConfiguration rodaConfiguration = null;
  private RodaConstants.NodeType nodeType;
  private RodaConstants.DistributedModeType distributedModeType;
  private List<String> configurationFiles = null;
  private boolean configSymbolicLinksAllowed;
  private Path rodaHomePath;
  private Path storagePath;
  private Path indexDataPath;
  private Path configPath;
  private Path dataPath;
  private Path logPath;
  private Path exampleConfigPath;
  private Path defaultPath;
  private Path reportPath;
  private Path synchronizationDirectoryPath;
  private Map<String, Map<String, String>> rodaPropertiesCache = null;

  /**
   * Cache of shared configuration properties
   *
   * @see #getRodaSharedConfigurationProperties
   */
  private Map<String, List<String>> rodaSharedConfigurationPropertiesCache = null;

  /**
   * Shared configuration and message properties (cache). Includes properties from
   * {@code rodaConfiguration} and translations from ServerMessages, filtered by
   * the {@code ui.sharedProperties.*} properties in {@code roda-wui.properties}.
   *
   * This cache provides the complete set of properties to be shared with the
   * client browser.
   */
  private LoadingCache<Locale, Map<String, List<String>>> SHARED_PROPERTIES_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<Locale, Map<String, List<String>>>() {
      @Override
      public Map<String, List<String>> load(Locale locale) {
        Map<String, List<String>> sharedProperties = new HashMap<>(getRodaSharedConfigurationProperties());
        Messages messages = getI18NMessages(locale);

        if (messages != null) {
          List<String> prefixes = getRodaConfigurationAsList("ui.sharedProperties.whitelist.messages.prefix");
          for (String prefix : prefixes) {
            Map<String, String> translations = messages.getTranslations(prefix, String.class, false);
            for (Map.Entry<String, String> translationEntry : translations.entrySet()) {
              sharedProperties.put("i18n." + translationEntry.getKey(),
                Collections.singletonList(translationEntry.getValue()));
            }
          }

          List<String> properties = getRodaConfigurationAsList("ui.display.properties.tika.fixed");
          for (String propertyKey : properties) {
            if (messages.containsTranslation(propertyKey)) {
              sharedProperties.put("i18n." + propertyKey,
                Collections.singletonList(messages.getTranslation(propertyKey)));
            }
          }
        }

        return sharedProperties;
      }
    });

  private LoadingCache<Locale, Messages> I18N_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<Locale, Messages>() {
      @Override
      public Messages load(Locale locale) throws Exception {
        return new Messages(locale, getConfigPath().resolve(RodaConstants.CORE_I18N_FOLDER));
      }
    });

  public static ConfigurationManager getInstance() {
    if (instance == null) {
      instance = new ConfigurationManager();
    }
    return instance;
  }

  public boolean isInstantiated() {
    return instantiated;
  }

  public boolean isInstantiatedWithoutErrors() {
    return instantiatedWithoutErrors;
  }

  public Configuration getRodaConfiguration() {
    return rodaConfiguration;
  }

  public RodaConstants.NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(RodaConstants.NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public RodaConstants.DistributedModeType getDistributedModeType() {
    return distributedModeType;
  }

  public void setDistributedModeType(RodaConstants.DistributedModeType distributedModeType) {
    this.distributedModeType = distributedModeType;
  }

  public boolean isConfigSymbolicLinksAllowed() {
    return configSymbolicLinksAllowed;
  }

  public void setConfigSymbolicLinksAllowed(boolean configSymbolicLinksAllowed) {
    this.configSymbolicLinksAllowed = configSymbolicLinksAllowed;
  }

  public Path getRodaHomePath() {
    return rodaHomePath;
  }

  public void setRodaHomePath(Path rodaHomePath) {
    this.rodaHomePath = rodaHomePath;
  }

  public Path getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(Path storagePath) {
    this.storagePath = storagePath;
  }

  public Path getIndexDataPath() {
    return indexDataPath;
  }

  public void setIndexDataPath(Path indexDataPath) {
    this.indexDataPath = indexDataPath;
  }

  public Path getConfigPath() {
    return configPath;
  }

  public void setConfigPath(Path configPath) {
    this.configPath = configPath;
  }

  public Path getDataPath() {
    return dataPath;
  }

  public void setDataPath(Path dataPath) {
    this.dataPath = dataPath;
  }

  public Path getLogPath() {
    return logPath;
  }

  public void setLogPath(Path logPath) {
    this.logPath = logPath;
  }

  public Path getExampleConfigPath() {
    return exampleConfigPath;
  }

  public void setExampleConfigPath(Path exampleConfigPath) {
    this.exampleConfigPath = exampleConfigPath;
  }

  public Path getDefaultPath() {
    return defaultPath;
  }

  public void setDefaultPath(Path defaultPath) {
    this.defaultPath = defaultPath;
  }

  public Path getReportPath() {
    return reportPath;
  }

  public void setReportPath(Path reportPath) {
    this.reportPath = reportPath;
  }

  public Path getSynchronizationDirectoryPath() {
    return synchronizationDirectoryPath;
  }

  public void setSynchronizationDirectoryPath(Path synchronizationDirectoryPath) {
    this.synchronizationDirectoryPath = synchronizationDirectoryPath;
  }

  private List<String> CONFIGURATIONS = new ArrayList<>(Arrays.asList("roda-core.properties", "roda-roles.properties",
    "roda-permissions.properties", "roda-instance.properties"));

  public void addDefaultConfiguration(String configuration) {
    CONFIGURATIONS.add(configuration);
    LOGGER.info("Added configuration: '{}'", configuration);
  }

  public void loadConfiguration() throws ConfigurationException {
    if (!instantiated) {

      // basic settings
      configSymbolicLinksAllowed = !Boolean
        .parseBoolean(System.getenv(RodaConstants.ENV_CONFIG_SYMBOLIC_LINKS_FORBIDDEN));

      // determine RODA HOME
      rodaHomePath = determineRodaHomePath();
      LOGGER.debug("RODA HOME is {}", rodaHomePath);

      // load core configurations
      rodaPropertiesCache = new HashMap<>();
      rodaConfiguration = new CompositeConfiguration();
      configurationFiles = new ArrayList<>();

      for (String configuration : CONFIGURATIONS) {
        addConfiguration(configuration);
        LOGGER.debug("Loaded {}", configuration);
      }

      instantiated = true;
    }
  }

  public void addConfiguration(String configurationFile) throws ConfigurationException {
    Configuration configuration = getConfiguration(configurationFile);
    rodaConfiguration.addConfiguration(configuration);
    configurationFiles.add(configurationFile);
  }

  public void addExternalConfiguration(Path configurationPath) throws ConfigurationException {
    Configuration configuration = getExternalConfiguration(configurationPath);
    rodaConfiguration.addConfiguration(configuration);
  }

  private PropertiesConfiguration initConfiguration() {
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setDelimiterParsingDisabled(true);
    propertiesConfiguration.setEncoding(RodaConstants.DEFAULT_ENCODING);
    return propertiesConfiguration;
  }

  private Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path config = getConfigPath().resolve(configurationFile);

    NodeCombiner combiner = new MergeCombiner();
    CombinedConfiguration cc = new CombinedConfiguration(combiner);

    if (FSUtils.exists(config)) {
      cc.addConfiguration(getExternalConfiguration(config));
    }

    cc.addConfiguration(getInternalConfiguration(configurationFile));

    // do variable interpolation
    Configuration configuration = cc.interpolatedConfiguration();

    return configuration;
  }

  private PropertiesConfiguration getInternalConfiguration(String configurationFile) throws ConfigurationException {
    PropertiesConfiguration propertiesConfiguration = initConfiguration();
    InputStream inputStream = ConfigurationManager.class
      .getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
    if (inputStream != null) {
      LOGGER.trace("Loading configuration from classpath {}", configurationFile);
      propertiesConfiguration.load(inputStream);

    } else {
      LOGGER.trace("Configuration {} doesn't exist", configurationFile);
    }

    return propertiesConfiguration;
  }

  private PropertiesConfiguration getExternalConfiguration(Path config) throws ConfigurationException {
    PropertiesConfiguration propertiesConfiguration = initConfiguration();
    LOGGER.trace("Loading configuration from file {}", config);
    propertiesConfiguration.load(config.toFile());
    RodaPropertiesReloadStrategy rodaPropertiesReloadStrategy = new RodaPropertiesReloadStrategy();
    rodaPropertiesReloadStrategy.setRefreshDelay(5000);
    propertiesConfiguration.setReloadingStrategy(rodaPropertiesReloadStrategy);

    return propertiesConfiguration;
  }

  public String getConfigurationKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }

  public String getRodaConfigurationAsString(String... keyParts) {
    return rodaConfiguration.getString(getConfigurationKey(keyParts));
  }

  public int getRodaConfigurationAsInt(int defaultValue, String... keyParts) {
    return rodaConfiguration.getInt(getConfigurationKey(keyParts), defaultValue);
  }

  public List<String> getRodaConfigurationAsList(String... keyParts) {
    String[] array = rodaConfiguration.getStringArray(getConfigurationKey(keyParts));
    return Arrays.stream(array).filter(StringUtils::isNotBlank).collect(Collectors.toList());
  }

  public String getConfigurationString(String key, String defaultValue) {
    String envKey = "RODA_" + key.toUpperCase().replace('.', '_');
    String value = System.getenv(envKey);
    if (value == null) {
      value = rodaConfiguration.getString(key, defaultValue);

      if (value.startsWith("${env:")) {
        // if value is a non-interpolated env variable consider default
        value = defaultValue;
      }
    }

    return value;
  }

  public URL getConfigurationFile(String configurationFile) {
    Path config = configPath.resolve(configurationFile);
    URL configUri;
    if (FSUtils.exists(config) && !FSUtils.isDirectory(config) && checkPathIsWithin(config, getConfigPath())) {
      try {
        configUri = config.toUri().toURL();
      } catch (MalformedURLException e) {
        LOGGER.error("Configuration {} is malformed: {}", configurationFile, e.getMessage());
        configUri = null;
      }
    } else {
      URL resource = ConfigurationManager.class
        .getResource("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
      if (resource != null) {
        configUri = resource;
      } else {
        LOGGER.trace("Configuration {} doesn't exist", configurationFile);
        configUri = null;
      }
    }

    return configUri;
  }

  public InputStream getScopedConfigurationFileAsStream(Path relativeConfigPath, String untrustedUserPath)
    throws GenericException {

    // security checks
    if (relativeConfigPath.isAbsolute()) {
      throw new GenericException("Relative config path must be relative");
    }

    Path normalizedBasePath = getConfigPath().resolve(relativeConfigPath).normalize();

    if (!normalizedBasePath.startsWith(getConfigPath())) {
      throw new GenericException(String.format("Relative config path %1$s must be within config path %2$s",
        normalizedBasePath, getConfigPath()));
    }

    Path finalPath = normalizedBasePath.resolve(untrustedUserPath).normalize();

    if (!checkPathIsWithin(finalPath, normalizedBasePath)) {
      throw new GenericException(
        String.format("Untrusted user path %1$s must be within base path %2$s", finalPath, normalizedBasePath));
    }

    String configurationFile = relativeConfigPath.resolve(untrustedUserPath).toString();
    return getConfigurationFileAsStream(getConfigPath(), configurationFile);
  }

  public InputStream getConfigurationFileAsStream(String configurationFile) {
    return getConfigurationFileAsStream(getConfigPath(), configurationFile);
  }

  public InputStream getConfigurationFileAsStream(Path baseConfigPath, String configurationFile) {
    Path configFile = baseConfigPath.resolve(configurationFile);
    InputStream inputStream = null;
    try {
      if (FSUtils.exists(configFile) && !FSUtils.isDirectory(configFile)
        && checkPathIsWithin(configFile, baseConfigPath)) {
        inputStream = Files.newInputStream(configFile);
        LOGGER.trace("Loading configuration from file {}", configFile);
      }
    } catch (IOException e) {
      // do nothing
    }

    if (inputStream == null) {
      Path relativizedPath = getConfigPath().getParent().relativize(baseConfigPath);
      inputStream = ConfigurationManager.class
        .getResourceAsStream("/" + relativizedPath.toString() + "/" + configurationFile);
      LOGGER.trace("Loading configuration from classpath {}", configurationFile);
    }

    return inputStream;
  }

  public InputStream getConfigurationFileAsStream(String configurationFile, String fallbackConfigurationFile) {
    InputStream inputStream = getConfigurationFileAsStream(getConfigPath(), configurationFile);
    if (inputStream == null) {
      inputStream = getConfigurationFileAsStream(getConfigPath(), fallbackConfigurationFile);
    }
    return inputStream;
  }

  public boolean checkPathIsWithin(Path path, Path folder) {
    return checkPathIsWithin(path, folder, configSymbolicLinksAllowed);
  }

  private boolean checkPathIsWithin(Path path, Path folder, boolean allowSymbolicLinks) {
    boolean ret = true;

    Path absolutePath = path.toAbsolutePath();

    // check against real path
    if (!allowSymbolicLinks) {
      Path realPath;
      try {
        realPath = absolutePath.toRealPath();
        ret &= realPath.isAbsolute();
        ret &= realPath.startsWith(folder.toAbsolutePath());
      } catch (IOException e) {
        LOGGER.warn("Error checking for path transversal", e);
        ret = false;
      }
    }

    // check against normalized path
    Path normalized = absolutePath.normalize();
    ret &= normalized.isAbsolute();
    ret &= normalized.startsWith(folder);

    return ret;
  }

  private Path determineRodaHomePath() {
    Path rodaHomePath;
    if (System.getProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY) != null) {
      rodaHomePath = Paths.get(System.getProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY));
    } else if (System.getenv(RodaConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE) != null) {
      rodaHomePath = Paths.get(System.getenv(RodaConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE));
    } else {
      // last attempt (using user home and hidden directory called .roda)
      String userHome = System.getProperty("user.home");
      rodaHomePath = Paths.get(userHome, ".roda");
      if (!FSUtils.exists(rodaHomePath)) {
        try {
          Files.createDirectories(rodaHomePath);
        } catch (IOException e) {
          throw new RuntimeException("Unable to create RODA HOME " + rodaHomePath + ". Aborting...", e);
        }
      }
    }
    // set roda.home in order to correctly configure logging even if no
    // property has been defined
    System.setProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, rodaHomePath.toString());

    // instantiate essential directories
    configPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_CONFIG_FOLDER);
    exampleConfigPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_EXAMPLE_CONFIG_FOLDER);
    defaultPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_DEFAULT_FOLDER);
    dataPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_DATA_FOLDER);
    logPath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_LOG_FOLDER);
    storagePath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_STORAGE_FOLDER);
    indexDataPath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_INDEX_FOLDER);
    reportPath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_REPORT_FOLDER);

    return rodaHomePath;
  }

  private Path getEssentialDirectoryPath(Path basePath, String directoryName) {
    String configuredPath = System.getenv(RodaConstants.CORE_ESSENTIAL_DIRECTORY_PREFIX + directoryName.toUpperCase());

    Path ret;

    if (StringUtils.isNotBlank(configuredPath)) {
      ret = Paths.get(FilenameUtils.normalize(configuredPath));
    } else {
      ret = basePath.resolve(directoryName);
    }

    return ret;
  }

  public void configureLogback() {
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      configurator.doConfigure(getConfigurationFile("logback.xml"));
    } catch (JoranException e) {
      LOGGER.error("Error configuring logback", e);
    }
  }

  /**
   * Gets (with caching) the shared configuration properties from
   * {@code rodaConfiguration}.
   *
   * The properties that should be shared with the client browser are defined by
   * the {@code ui.sharedProperties.*} properties in {@code roda-wui.properties}.
   *
   * @return The configuration properties that should be shared with the client
   *         browser.
   */
  private Map<String, List<String>> getRodaSharedConfigurationProperties() {
    if (rodaSharedConfigurationPropertiesCache == null) {
      rodaSharedConfigurationPropertiesCache = new HashMap<>();
      Configuration configuration = getRodaConfiguration();

      List<String> prefixes = getRodaConfigurationAsList("ui.sharedProperties.whitelist.configuration.prefix");

      rodaSharedConfigurationPropertiesCache.put(RodaConstants.RODA_NODE_TYPE_KEY,
        Collections.singletonList(getNodeType().toString()));

      rodaSharedConfigurationPropertiesCache.put(RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY,
        Collections.singletonList(getDistributedModeType().toString()));

      rodaSharedConfigurationPropertiesCache.put(RodaConstants.CORE_SYNCHRONIZATION_FOLDER,
        Collections.singletonList(getSynchronizationDirectoryPath().toString()));

      Iterator<String> keys = configuration.getKeys();
      while (keys.hasNext()) {
        String key = keys.next();
        for (String prefix : prefixes) {
          if (key.startsWith(prefix + ".")) {
            rodaSharedConfigurationPropertiesCache.put(key, getRodaConfigurationAsList(key));
            break;
          }
        }
      }

      List<String> properties = getRodaConfigurationAsList("ui.sharedProperties.whitelist.configuration.property");
      for (String propertyKey : properties) {
        if (configuration.containsKey(propertyKey)) {
          rodaSharedConfigurationPropertiesCache.put(propertyKey, getRodaConfigurationAsList(propertyKey));
        }
      }
    }
    return rodaSharedConfigurationPropertiesCache;
  }

  public Map<String, List<String>> getRodaSharedProperties(Locale locale) {
    checkForChangesInI18N();
    try {
      return SHARED_PROPERTIES_CACHE.get(locale);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not load shared properties", e);
      return Collections.emptyMap();
    }
  }

  private void checkForChangesInI18N() {
    // i18n is cached and that cache is re-done when changes occur to
    // roda-*.properties (for convenience)
    getRodaConfiguration().getString("");
  }

  public Messages getI18NMessages(Locale locale) {
    checkForChangesInI18N();
    try {
      return I18N_CACHE.get(locale);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not load messages", e);
      return null;
    }
  }

  public Map<String, String> getPropertiesFromCache(String cacheName, List<String> prefixesToCache) {
    if (rodaPropertiesCache.get(cacheName) == null) {
      fillInPropertiesToCache(cacheName, prefixesToCache);
    }
    return rodaPropertiesCache.get(cacheName);
  }

  private void fillInPropertiesToCache(String cacheName, List<String> prefixesToCache) {
    if (rodaPropertiesCache.get(cacheName) == null) {
      HashMap<String, String> newCacheEntry = new HashMap<>();

      Configuration configuration = getRodaConfiguration();
      Iterator<String> keys = configuration.getKeys();
      while (keys.hasNext()) {
        String key = String.class.cast(keys.next());
        String value = configuration.getString(key, "");
        for (String prefixToCache : prefixesToCache) {
          if (key.startsWith(prefixToCache)) {
            newCacheEntry.put(key, value);
            break;
          }
        }
      }

      rodaPropertiesCache.put(cacheName, newCacheEntry);
    }
  }

  /**
   * Try to get property from 1) system property (passed in command-line via -D;
   * if property does not start by "roda.", it will be prepended); 2) environment
   * variable (upper case, replace '.' by '_' and if property does not start by
   * "RODA_" after replacements, it will be prepended); 3) RODA configuration
   * files (with original property value, ensuring that it does not start by
   * "roda."); 4) return default value
   *
   * <p>
   * Example 1: for property = 'roda.node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   * <p>
   * Example 2: for property = 'node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   */
  public String getProperty(String property, String defaultValue) {
    String sysProperty = property;
    if (!sysProperty.startsWith("roda.")) {
      sysProperty = "roda." + sysProperty;
    }
    String ret = System.getProperty(sysProperty);
    if (ret == null) {
      String envProperty = sysProperty.toUpperCase().replace('.', '_');
      ret = System.getenv(envProperty);
      if (ret == null && getRodaConfiguration() != null) {
        String confProperty = property.replaceFirst("^roda.", "");
        ret = getRodaConfiguration().getString(confProperty, defaultValue);
      }
    }
    if (ret == null) {
      ret = defaultValue;
    }
    return ret;
  }

  /**
   * Try to get property from 1) system property (passed in command-line via -D;
   * if property does not start by "roda.", it will be prepended); 2) environment
   * variable (upper case, replace '.' by '_' and if property does not start by
   * "RODA_" after replacements, it will be prepended); 3) RODA configuration
   * files (with original property value, ensuring that it does not start by
   * "roda."); 4) return default value
   *
   * <p>
   * Example 1: for property = 'roda.node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   * <p>
   * Example 2: for property = 'node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   */
  public boolean getProperty(String property, boolean defaultValue) {
    return Boolean.parseBoolean(getProperty(property, Boolean.toString(defaultValue)));
  }

  /**
   * Try to get property from 1) system property (passed in command-line via -D;
   * if property does not start by "roda.", it will be prepended); 2) environment
   * variable (upper case, replace '.' by '_' and if property does not start by
   * "RODA_" after replacements, it will be prepended); 3) RODA configuration
   * files (with original property value, ensuring that it does not start by
   * "roda."); 4) return default value
   *
   * <p>
   * Example 1: for property = 'roda.node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   * <p>
   * Example 2: for property = 'node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   */
  public int getProperty(String property, int defaultValue) {
    return Integer.parseInt(getProperty(property, Integer.toString(defaultValue)));
  }

  public void clearRodaCachableObjectsAfterConfigurationChange() {
    rodaSharedConfigurationPropertiesCache = null;
    rodaPropertiesCache.clear();
    I18N_CACHE.invalidateAll();
    SHARED_PROPERTIES_CACHE.invalidateAll();
  }
}
