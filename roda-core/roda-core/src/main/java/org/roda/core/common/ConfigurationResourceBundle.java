package org.roda.core.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;

import com.google.common.collect.Iterators;

public class ConfigurationResourceBundle extends ResourceBundle {

  private Configuration configuration;
  private Locale locale;
  private Enumeration<String> keys;

  public ConfigurationResourceBundle(CombinedConfiguration configuration, Locale locale) {
    super();
    
    // List all keys before interpolating
    Collection<String> keyList = new ArrayList<>();
    Iterators.addAll(keyList, configuration.getKeys());
    this.keys = Collections.enumeration(keyList);

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
  public Enumeration<String> getKeys() {
    return keys;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}
