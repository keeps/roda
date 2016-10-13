/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DescriptionLevelConfiguration implements Serializable {

  private static final long serialVersionUID = 3476633742750492225L;
  
  private Map<String,String> translations;

  private List<String> classificationPlanLevels;

  private String ghostClass;

  private String defaultClass;

  private String representationClass;

  private String representationFolderClass;

  private String representationFileClass;
  
  private Map<String, String> representationTypesIcons;

  private Map<String, String> levelIcons;

  public DescriptionLevelConfiguration() {
  }

  public List<String> getClassificationPlanLevels() {
    return classificationPlanLevels;
  }

  public void setClassificationPlanLevels(List<String> classificationPlanLevels) {
    this.classificationPlanLevels = classificationPlanLevels;
  }

  public String getGhostClass() {
    return ghostClass;
  }

  public void setGhostClass(String ghostClass) {
    this.ghostClass = ghostClass;
  }

  public String getDefaultClass() {
    return defaultClass;
  }

  public void setDefaultClass(String defaultClass) {
    this.defaultClass = defaultClass;
  }

  public String getRepresentationClass() {
    return representationClass;
  }

  public void setRepresentationClass(String representationClass) {
    this.representationClass = representationClass;
  }

  public String getRepresentationFolderClass() {
    return representationFolderClass;
  }

  public void setRepresentationFolderClass(String representationFolderClass) {
    this.representationFolderClass = representationFolderClass;
  }

  public String getRepresentationFileClass() {
    return representationFileClass;
  }

  public void setRepresentationFileClass(String representationFileClass) {
    this.representationFileClass = representationFileClass;
  }

  public Map<String, String> getLevelIcons() {
    return levelIcons;
  }

  public void setLevelIcons(Map<String, String> levelIcons) {
    this.levelIcons = levelIcons;
  }

  public Map<String, String> getTranslations() {
    return translations;
  }

  public void setTranslations(Map<String, String> translations) {
    this.translations = translations;
  }

  public Map<String, String> getRepresentationTypesIcons() {
    return representationTypesIcons;
  }

  public void setRepresentationTypesIcons(Map<String, String> representationTypesIcons) {
    this.representationTypesIcons = representationTypesIcons;
  }
}
