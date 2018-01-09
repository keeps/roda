/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.ri.RelationObjectType;

public class RelationTypeTranslationsBundle implements Serializable {
  private static final long serialVersionUID = -9071364356622542592L;

  // RelationType -> <Value, Translation>
  private Map<RelationObjectType, Map<String, String>> translations = new HashMap<>();
  private Map<String, String> inverses = new HashMap<>();
  private Map<String, String> inverseTranslations = new HashMap<>();

  public RelationTypeTranslationsBundle() {
    super();
  }

  public RelationTypeTranslationsBundle(Map<RelationObjectType, Map<String, String>> translations,
    Map<String, String> inverses, Map<String, String> inverseTranslations) {
    super();
    this.translations = translations;
    this.inverses = inverses;
    this.inverseTranslations = inverseTranslations;
  }

  public Map<RelationObjectType, Map<String, String>> getTranslations() {
    return translations;
  }

  public void setTranslations(Map<RelationObjectType, Map<String, String>> translations) {
    this.translations = translations;
  }

  public Map<String, String> getInverses() {
    return inverses;
  }

  public void setInverses(Map<String, String> inverses) {
    this.inverses = inverses;
  }

  public Map<String, String> getInverseTranslations() {
    return inverseTranslations;
  }

  public void setInverseTranslations(Map<String, String> inverseTranslations) {
    this.inverseTranslations = inverseTranslations;
  }
}
