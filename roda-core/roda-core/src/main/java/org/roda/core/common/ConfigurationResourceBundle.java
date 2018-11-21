/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;

public class ConfigurationResourceBundle extends ResourceBundle {

  private Configuration configuration;
  private Locale locale;
  private Set<String> keys;

  public ConfigurationResourceBundle(CombinedConfiguration configuration, Locale locale) {
    super();

    // List all keys before interpolating
    this.keys = new HashSet<>();
    configuration.getKeys().forEachRemaining(x -> keys.add(x));

    this.configuration = configuration.interpolatedConfiguration();
    this.locale = locale;

  }

  @Override
  public Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return configuration.getProperty(key);
  }

  @Override
  protected Set<String> handleKeySet() {
    return keys;
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(keys);
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}
