/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messages {
  private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

  private static final String MESSAGES_BUNDLE = "ServerMessages";
  private final Locale locale;
  private final ResourceBundle resourceBundle;
  private final Map<String, Map<String, ?>> translationsCache;

  public Messages(Locale locale, Path folder) {
    this.locale = locale;
    this.resourceBundle = ResourceBundle.getBundle(MESSAGES_BUNDLE, locale, new FolderBasedUTF8Control(folder));
    this.translationsCache = new HashMap<>();
  }

  /**
   * Get translation
   *
   * @param key
   * @return
   */
  public String getTranslation(String key) {
    try {
      // 1. Try core ServerMessages first
      return resourceBundle.getString(key);
    } catch (MissingResourceException e) {
      // 2. Fallback to loaded plugin Messages
      for (Function<Locale, ResourceBundle> provider : RodaCoreFactory.getAllPluginMessageProviders()) {
        ResourceBundle pluginBundle = provider.apply(this.locale);
        if (pluginBundle != null && pluginBundle.containsKey(key)) {
          return pluginBundle.getString(key);
        }
      }
      // 3. Throw original exception if missing everywhere
      throw e;
    }
  }

  public String getTranslationWithArgs(String key, Object... args) {
    return String.format(getTranslation(key), args);
  }

  public String getTranslation(String key, String fallback) {
    String ret;
    try {
      ret = getTranslation(key);
    } catch (MissingResourceException e) {
      ret = fallback;
    }
    return ret;
  }

  public boolean containsTranslation(String key) {
    if (resourceBundle.containsKey(key)) {
      return true;
    }
    for (Function<Locale, ResourceBundle> provider : RodaCoreFactory.getAllPluginMessageProviders()) {
      ResourceBundle pluginBundle = provider.apply(this.locale);
      if (pluginBundle != null && pluginBundle.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * prefix will be replaced by "i18n." for simplicity purposes
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> Map<String, T> getTranslations(String prefix, Class<T> valueClass,
    boolean replacePrefixFromKey) {
    // try cache first
    if (translationsCache.get(prefix) != null) {
      return (Map<String, T>) translationsCache.get(prefix);
    }

    Map<String, T> map = new HashMap<>();
    String fullPrefix = prefix + ".";

    // 1. Load keys from main Core bundle
    populateTranslationsMap(map, this.resourceBundle, fullPrefix, replacePrefixFromKey, valueClass);

    // 2. Merge keys from all Plugin bundles (Core keys take precedence if conflicts
    // occur)
    for (Function<Locale, ResourceBundle> provider : RodaCoreFactory.getAllPluginMessageProviders()) {
      ResourceBundle pluginBundle = provider.apply(this.locale);
      populateTranslationsMap(map, pluginBundle, fullPrefix, replacePrefixFromKey, valueClass);
    }

    // cache it
    translationsCache.put(prefix, map);
    return map;
  }

  /**
   * Helper method to populate translation map from a specific ResourceBundle
   */
  private <T extends Object> void populateTranslationsMap(Map<String, T> map, ResourceBundle bundle, String fullPrefix,
    boolean replacePrefixFromKey, Class<T> valueClass) {

    if (bundle == null)
      return;

    Enumeration<String> keys = bundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      if (key.startsWith(fullPrefix)) {
        String finalKey = replacePrefixFromKey ? key.replaceFirst(Pattern.quote(fullPrefix), "i18n.") : key;
        // Use putIfAbsent to ensure Core translations don't get overwritten by plugin
        // translations
        map.putIfAbsent(finalKey, valueClass.cast(bundle.getString(key)));
      }
    }
  }

  private class FolderBasedUTF8Control extends Control {
    private static final String CONFIG_I18N_PATH = "/config/i18n/";
    private Path folder;

    public FolderBasedUTF8Control(Path folder) {
      this.folder = folder;
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
      // ask not to cache
      return TTL_DONT_CACHE;
    }

    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
      if (baseName == null) {
        throw new NullPointerException();
      }
      // 20160712 hsilva: the following line is needed otherwise default locale
      // is used and this can be incoherent with other parts of the code where
      // the default locale is ENGLISH
      Locale defaultLocale = Locale.ENGLISH;
      return locale.equals(defaultLocale) ? null : defaultLocale;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {

      if (!"java.properties".equals(format)) {
        return null;
      }

      String bundleName = toBundleName(baseName, locale) + ".properties";
      ResourceBundle bundle = null;

      try {
        Path bundlePath = folder.resolve(bundleName);

        NodeCombiner combiner = new MergeCombiner();
        CombinedConfiguration cc = new CombinedConfiguration(combiner);
        boolean hasExternal = false;
        boolean hasInternal = false;

        // external
        if (Files.exists(bundlePath)) {
          PropertiesConfiguration pce = new PropertiesConfiguration();
          pce.read(Files.newBufferedReader(bundlePath));
          cc.addConfiguration(pce);
          hasExternal = true;
        }

        // internal
        try (InputStream pcis = this.getClass().getResourceAsStream(CONFIG_I18N_PATH + bundleName);) {
          if (pcis != null) {
            String pciss = IOUtils.toString(pcis, Charset.defaultCharset());
            PropertiesConfiguration pci = new PropertiesConfiguration();
            pci.read(new StringReader(pciss));
            cc.addConfiguration(pci);
            hasInternal = true;

          }
        }

        // create bundle
        if (hasExternal || hasInternal) {
          bundle = new ConfigurationResourceBundle(cc, locale);
        }

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading " + bundleName, e);
      }
      return bundle;
    }
  }

}
