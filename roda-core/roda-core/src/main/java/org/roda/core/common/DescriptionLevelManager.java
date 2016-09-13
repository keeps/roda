/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.roda.core.data.descriptionLevels.DescriptionLevel;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for loading description levels.
 * 
 * @author Hélder Silva
 * @author Luis Faria
 */
public class DescriptionLevelManager implements Serializable {
  private static final long serialVersionUID = 9038357012292858571L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionLevelManager.class);

  private static final String GHOST_LEVEL_ICON = "level.ghost.icon";
  private static final String DEFAULT_LEVEL_ICON = "level.default.icon";
  
  private static final String REPRESENTATION_ICON = "level.representation.icon";
  private static final String REPRESENTATION_FOLDER_ICON = "level.representation.folder.icon";
  private static final String REPRESENTATION_FILE_ICON = "level.representation.file.icon";

  
  private static final String LEVELS_WITH_REPRESENTATION = "levels_with_representation";
  private static final String LOCALES2 = "locales";
  private static final String LEVELS_ORDERED = "levels_ordered";

  private static List<String> LEVELS = new ArrayList<String>();
  private static List<String> REPRESENTATIONS = new ArrayList<String>();
  private static List<String> LOCALES = new ArrayList<String>();
  private static List<DescriptionLevel> DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  private static List<DescriptionLevel> REPRESENTATION_LEVELS = new ArrayList<DescriptionLevel>();
  private static List<DescriptionLevel> ALL_BUT_REPRESENTATION_LEVELS = new ArrayList<DescriptionLevel>();
  private static String GHOST_CLASS;
  private static String DEFAULT_CLASS;
  private static String REPRESENTATION_CLASS;
  private static String REPRESENTATION_FOLDER_CLASS;
  private static String REPRESENTATION_FILE_CLASS;

  public DescriptionLevelManager(Configuration descriptionLevels) throws RequestNotValidException {
    loadDescriptionLevelHierarchy(descriptionLevels);
  }

  public DescriptionLevelManager() {
  }

  public List<String> getLevels() {
    return LEVELS;
  }

  public List<DescriptionLevel> getDescriptionLevels() {
    return DESCRIPTION_LEVELS;
  }

  private void loadDescriptionLevelHierarchy(Configuration configuration) throws RequestNotValidException {
    DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
    REPRESENTATION_LEVELS = new ArrayList<DescriptionLevel>();
    ALL_BUT_REPRESENTATION_LEVELS = new ArrayList<DescriptionLevel>();
    LEVELS = RodaUtils.copyList(configuration.getList(LEVELS_ORDERED));
    LOCALES = RodaUtils.copyList(configuration.getList(LOCALES2));
    REPRESENTATIONS = RodaUtils.copyList(configuration.getList(LEVELS_WITH_REPRESENTATION));

    for (String stringLevel : LEVELS) {
      DescriptionLevel level = new DescriptionLevel();
      Map<String, String> labels = new HashMap<String, String>();
      for (String locale : LOCALES) {
        String label = configuration.getString("label." + locale + "." + stringLevel);
        if (label != null) {
          labels.put(locale, label);
        }
      }
      level.setLabels(labels);
      level.setLevel(stringLevel);
      level.setIconClass(configuration.getString("level." + stringLevel + ".icon"));
      DESCRIPTION_LEVELS.add(level);
      if (REPRESENTATIONS.contains(stringLevel)) {
        REPRESENTATION_LEVELS.add(level);
      } else {
        ALL_BUT_REPRESENTATION_LEVELS.add(level);
      }
    }
    if (configuration.containsKey(DEFAULT_LEVEL_ICON)) {
      DEFAULT_CLASS = configuration.getString(DEFAULT_LEVEL_ICON);
    } else {
      DEFAULT_CLASS = "fa fa-file";
    }
    if (configuration.containsKey(GHOST_LEVEL_ICON)) {
      GHOST_CLASS = configuration.getString(GHOST_LEVEL_ICON);
    } else {
      GHOST_CLASS = "fa fa-snapshat";
    }
    
    if(configuration.containsKey(REPRESENTATION_ICON)){
      REPRESENTATION_CLASS = configuration.getString(REPRESENTATION_ICON);
    }else{
      REPRESENTATION_CLASS = "fa fa-picture-o";
    }
    
    if(configuration.containsKey(REPRESENTATION_FOLDER_ICON)){
      REPRESENTATION_FOLDER_CLASS = configuration.getString(REPRESENTATION_FOLDER_ICON);
    }else{
      REPRESENTATION_FOLDER_CLASS = "fa fa-folder-o";
    }
    
    if(configuration.containsKey(REPRESENTATION_FILE_ICON)){
      REPRESENTATION_FILE_CLASS = configuration.getString(REPRESENTATION_FILE_ICON);
    }else{
      REPRESENTATION_FILE_CLASS = "fa fa-file-o";
    }
  }

  public List<DescriptionLevel> getAllButRepresentationsDescriptionLevels() {
    return ALL_BUT_REPRESENTATION_LEVELS;
  }

  public String getDefaultClass() {
    return DEFAULT_CLASS;
  }

  public String getGhostClass() {
    return GHOST_CLASS;
  }

  public String getRepresentationClass() {
    return REPRESENTATION_CLASS;
  }

  public String getRepresentationFileClass() {
    return REPRESENTATION_FILE_CLASS;
  }

  public String getRepresentationFolderClass() {
    return REPRESENTATION_FOLDER_CLASS;
  }
}
