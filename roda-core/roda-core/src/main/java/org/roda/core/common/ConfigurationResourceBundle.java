package org.roda.core.common;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.configuration2.Configuration;

import com.google.common.collect.Iterators;

public class ConfigurationResourceBundle extends ResourceBundle {

  protected Configuration configuration;
  protected Locale locale;

  public ConfigurationResourceBundle(Configuration configuration, Locale locale) {
    super();
    this.configuration = configuration;
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
    return Iterators.asEnumeration(configuration.getKeys());
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}
