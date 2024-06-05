package org.roda.core.data.v2.properties;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ObjectClassFields implements Serializable {

  @Serial
  private static final long serialVersionUID = -4695879031222737450L;

  private Map<String, List<String>> objectClassFieldsMap;
  private Map<String, String> translations;

  public ObjectClassFields() {
    this.objectClassFieldsMap = new HashMap<>();
    this.translations = new HashMap<>();
  }

  public Map<String, List<String>> getObjectClassFields() {
    return objectClassFieldsMap;
  }

  public void setObjectClassFields(Map<String, List<String>> objectClassFields) {
    this.objectClassFieldsMap = objectClassFields;
  }

  public Map<String, String> getTranslations() {
    return translations;
  }

  public void setTranslations(Map<String, String> translations) {
    this.translations = translations;
  }
}
