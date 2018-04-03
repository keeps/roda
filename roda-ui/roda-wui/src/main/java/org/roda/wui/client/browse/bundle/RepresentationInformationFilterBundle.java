/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepresentationInformationFilterBundle implements Serializable {
  private static final long serialVersionUID = 2188826656915820770L;

  private Map<String, List<String>> objectClassFields;
  private Map<String, String> translations;

  public RepresentationInformationFilterBundle() {
    this.objectClassFields = new HashMap<>();
    this.translations = new HashMap<>();
  }

  public Map<String, List<String>> getObjectClassFields() {
    return objectClassFields;
  }

  public void setObjectClassFields(Map<String, List<String>> objectClassFields) {
    this.objectClassFields = objectClassFields;
  }

  public Map<String, String> getTranslations() {
    return translations;
  }

  public void setTranslations(Map<String, String> translations) {
    this.translations = translations;
  }
}
