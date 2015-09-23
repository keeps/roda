package config.i18n.server;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class XSLTMessages {
  private static final String BUNDLE_NAME = "config.i18n.server.XSLTMessages"; //$NON-NLS-1$

  private ResourceBundle resourceBundle;

  /**
   * Create a new ingest list report messages translations for a defined locale
   * 
   * @param locale
   */
  public XSLTMessages(Locale locale) {
    this.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  /**
   * Get translation
   * 
   * @param key
   * @return
   */
  public String getString(String key) {
    return resourceBundle.getString(key);
  }
  
  public Map<String,String> getTranslations(String prefix){
    Map<String,String> map = new HashMap<String,String>();
    Enumeration<String> keys = resourceBundle.getKeys();
    String fullPrefix = prefix+".";
    while(keys.hasMoreElements()){
      String key = keys.nextElement();
      if(key.startsWith(fullPrefix)){
        map.put(key.replaceAll("[^A-Za-z0-9]", ""), resourceBundle.getString(key));
      }
    }
    return map;
  }
}
