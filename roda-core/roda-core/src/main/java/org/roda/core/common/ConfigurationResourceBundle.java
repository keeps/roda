package org.roda.core.common;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;

public class ConfigurationResourceBundle extends ResourceBundle {

  protected Configuration configuration;
  protected Locale locale;

  public ConfigurationResourceBundle(Configuration configuration, Locale locale) {
    this.configuration = configuration;
    this.locale = locale;
  }

  public Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return configuration.getProperty(key);
  }

  public Enumeration<String> getKeys() {
    Set<String> myKeys = handleKeySet();
    if (parent != null) {
      Enumeration<String> parentKeysEnum = parent.getKeys();
      while (parentKeysEnum.hasMoreElements()) {
        myKeys.add(parentKeysEnum.nextElement());
      }
    }
    return Collections.enumeration(myKeys);
  }

  protected Set<String> handleKeySet() {
    Iterator<String> iterator = configuration.getKeys();
    Set<String> keys = new HashSet<String>();
    while (iterator.hasNext()) {
      keys.add(iterator.next());
    }
    return keys;
  }

  @Override
  public void setParent(ResourceBundle parent) {
    super.setParent(parent);
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}
