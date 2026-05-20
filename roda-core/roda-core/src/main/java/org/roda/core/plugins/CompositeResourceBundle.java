package org.roda.core.plugins;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CompositeResourceBundle extends ResourceBundle {
  private final ResourceBundle primary;
  private final ResourceBundle fallback;

  public CompositeResourceBundle(ResourceBundle primary, ResourceBundle fallback) {
    this.primary = primary;
    this.fallback = fallback;
  }

  @Override
  protected Object handleGetObject(String key) {
    // 1. Try to find the key in the specific Plugin (AudioConverter)
    if (primary != null && primary.containsKey(key)) {
      return primary.getObject(key);
    }
    // 2. Fallback to the Library (AbstractConvertPlugin)
    if (fallback != null && fallback.containsKey(key)) {
      return fallback.getObject(key);
    }
    return null;
  }

  @Override
  public Enumeration<String> getKeys() {
    Set<String> keys = new HashSet<>();
    if (primary != null) {
      keys.addAll(Collections.list(primary.getKeys()));
    }
    if (fallback != null) {
      keys.addAll(Collections.list(fallback.getKeys()));
    }
    return Collections.enumeration(keys);
  }
}
