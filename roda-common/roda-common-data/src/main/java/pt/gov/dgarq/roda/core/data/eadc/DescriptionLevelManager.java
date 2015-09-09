package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility for loading description levels.
 * 
 * @author Hélder Silva
 * @author Luis Faria
 */
public class DescriptionLevelManager implements Serializable {
  private static final long serialVersionUID = 9038357012292858571L;

  // list of description levels (any order in this list it's not to
  // translate description level hierarchy, but instead to be for presentation
  // purposes (e.g. advanced search))
  private static List<String> LEVELS = new ArrayList<String>();
  private static List<DescriptionLevel> DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  private static List<DescriptionLevelInfo> DESCRIPTION_LEVELS_INFO = new ArrayList<DescriptionLevelInfo>();
  // root description levels
  private static List<DescriptionLevel> ROOT_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  // leaf description level
  @Deprecated
  private static List<DescriptionLevel> LEAF_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  // description levels which can have representations on it
  private static List<DescriptionLevel> REPRESENTATION_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  // all description levels without the representation description levels
  private static List<DescriptionLevel> ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
  // forward and backward representation levels mapping
  private static Map<String, List<DescriptionLevel>> PARENT_TO_CHILDREN_MAPPING = new HashMap<String, List<DescriptionLevel>>();
  private static Map<String, List<DescriptionLevel>> CHILDREN_TO_PARENT_MAPPING = new HashMap<String, List<DescriptionLevel>>();

  /** -------------------- CODE TO DELETE - END -------------------- */

  /**
   * Constructor that must be used to initialize the static content of this
   * class
   * 
   * @param descriptionLevels
   *          map read from the properties files
   * */
  public DescriptionLevelManager(Map<Object, Object> descriptionLevels) {
    loadDescriptionLevelHierarchy(descriptionLevels);
  }

  public DescriptionLevelManager() {
  }

  /**
   * Non-cloned version of the list containing description levels
   * */
  public static List<String> getLevels() {
    return LEVELS;
  }

  /**
   * Non-cloned version of the list containing description levels
   * */
  public static List<DescriptionLevel> getDescriptionLevels() {
    return DESCRIPTION_LEVELS;
  }

  /**
   * Non-cloned version of the list containing description levels info
   * */
  public static List<DescriptionLevelInfo> getDescriptionLevelsInfo() {
    return DESCRIPTION_LEVELS_INFO;
  }

  /**
   * Non-cloned version of the list containing child description levels from
   * specified parentLevel
   * */
  public static List<DescriptionLevel> getChildLevels(String parentLevel) {
    return PARENT_TO_CHILDREN_MAPPING.get(parentLevel);
  }

  /**
   * Non-cloned version of the list containing child description levels from
   * specified parentLevel
   * */
  public static List<DescriptionLevel> getChildLevels(DescriptionLevel parentLevel) {
    return PARENT_TO_CHILDREN_MAPPING.get(parentLevel.getLevel());
  }

  /**
   * Non-cloned version of the list containing parent description levels from
   * specified childLevel
   * */
  public static List<DescriptionLevel> getParentLevels(String childLevel) {
    return CHILDREN_TO_PARENT_MAPPING.get(childLevel);
  }

  /**
   * Non-cloned version of the list containing parent description levels from
   * specified childLevel
   * */
  public static List<DescriptionLevel> getParentLevels(DescriptionLevel childLevel) {
    return CHILDREN_TO_PARENT_MAPPING.get(childLevel.getLevel());
  }

  /**
   * Non-cloned version of the list containing root description levels
   * */
  public static List<DescriptionLevel> getRootDescriptionLevels() {
    return ROOT_DESCRIPTION_LEVELS;
  }

  public static String getFirstRootLevel() {
    if (ROOT_DESCRIPTION_LEVELS.get(0) != null) {
      return ROOT_DESCRIPTION_LEVELS.get(0).getLevel();
    } else {
      return "";
    }
  }

  public static DescriptionLevel getFirstRootDescriptionLevel() {
    return ROOT_DESCRIPTION_LEVELS.get(0);
  }

  /**
   * Non-cloned version of the list containing representation description levels
   * */
  public static List<DescriptionLevel> getRepresentationsDescriptionLevels() {
    return REPRESENTATION_DESCRIPTION_LEVELS;
  }

  /**
   * Non-cloned version of the list containing leaf description levels
   * */
  @Deprecated
  public static List<DescriptionLevel> getLeafDescriptionLevels() {
    return LEAF_DESCRIPTION_LEVELS;
  }

  @Deprecated
  public static String getFirstLeafLevel() {
    return LEAF_DESCRIPTION_LEVELS.get(0).getLevel();
  }

  @Deprecated
  public static DescriptionLevel getFirstLeafDescriptionLevel() {
    return LEAF_DESCRIPTION_LEVELS.get(0);
  }

  /**
   * Non-cloned version of the list containing all but representations
   * description levels
   * */
  public static List<DescriptionLevel> getAllButRepresentationsDescriptionLevels() {
    return ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS;
  }

  private Set<String> getAllDescendants(String level) {
    Set<String> descendants = new HashSet<String>();
    Set<String> visitedLevels = new HashSet<String>();
    for (DescriptionLevel descendant : PARENT_TO_CHILDREN_MAPPING.get(level)) {
      descendants.add(descendant.getLevel());
      descendants.addAll(getAllDescendants(descendant.getLevel(), visitedLevels));

    }
    return descendants;
  }

  private Set<String> getAllDescendants(String level, Set<String> visitedLevels) {
    Set<String> descendants = new HashSet<String>();
    if (!visitedLevels.contains(level)) {
      visitedLevels.add(level);
      descendants.add(level);
      for (DescriptionLevel descendant : PARENT_TO_CHILDREN_MAPPING.get(level)) {
        descendants.addAll(getAllDescendants(descendant.getLevel(), visitedLevels));
      }
    }
    return descendants;
  }

  private void loadDescriptionLevelHierarchy(Map<Object, Object> descriptionLevels) {

    // instantiate objects to contain the description levels (both String
    // and
    // DescriptionLevel flavors)
    LEVELS = new ArrayList<String>();
    DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
    DESCRIPTION_LEVELS_INFO = new ArrayList<DescriptionLevelInfo>();
    ROOT_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
    LEAF_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
    REPRESENTATION_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>();
    PARENT_TO_CHILDREN_MAPPING = new HashMap<String, List<DescriptionLevel>>();
    CHILDREN_TO_PARENT_MAPPING = new HashMap<String, List<DescriptionLevel>>();

    // temporary variables
    String key, value;
    List<DescriptionLevel> childLevels, tempList;

    for (Entry<Object, Object> entry : descriptionLevels.entrySet()) {

      key = (String) entry.getKey();
      value = (String) entry.getValue();

      if (key.equals("roots")) {
        ROOT_DESCRIPTION_LEVELS = splitChildLevelsAsObjects(value);
      } else if (key.equals("levels_ordered")) {
        LEVELS = splitList(value);
        for (String level : LEVELS) {
          final DescriptionLevelInfo levelInfo = new DescriptionLevelInfo();
          levelInfo.setLevel(level);
          DESCRIPTION_LEVELS_INFO.add(levelInfo);
          DESCRIPTION_LEVELS.add(levelInfo.getDescriptionLevel());
        }
      } else if (key.equals("representations")) {
        REPRESENTATION_DESCRIPTION_LEVELS = splitChildLevelsAsObjects(value);
      } else if (key.matches("^level\\.[^.]+")) {
        String level = key.substring("level.".length());

        // process information about child levels
        childLevels = splitChildLevelsAsObjects(value);
        PARENT_TO_CHILDREN_MAPPING.put(level, childLevels);

        if (childLevels.size() == 0) {
          LEAF_DESCRIPTION_LEVELS.add(new DescriptionLevel(level));
        }

        for (DescriptionLevel childrenLevel : childLevels) {
          tempList = CHILDREN_TO_PARENT_MAPPING.get(childrenLevel.getLevel());
          if (tempList == null) {
            tempList = new ArrayList<DescriptionLevel>();
            tempList.add(new DescriptionLevel(level));
            CHILDREN_TO_PARENT_MAPPING.put(childrenLevel.getLevel(), tempList);
          } else {
            if (!tempList.contains(level)) {
              tempList.add(new DescriptionLevel(level));
            }
            CHILDREN_TO_PARENT_MAPPING.put(childrenLevel.getLevel(), tempList);
          }
        }
      }
    }

    List<String> locales = splitList((String) descriptionLevels.get("locales"));
    for (DescriptionLevelInfo level : DESCRIPTION_LEVELS_INFO) {
      String category = (String) descriptionLevels.get("category." + level.getLevel());
      level.setCategory(DescriptionLevelCategory.valueOf(category));
      for (String locale : locales) {
        String label = (String) descriptionLevels.get("label." + locale + "." + level.getLevel());
        if (label != null) {
          level.setLabel(locale, label);
        }
      }
    }

    ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS = new ArrayList<DescriptionLevel>(DESCRIPTION_LEVELS);
    ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS.removeAll(DescriptionLevelManager.REPRESENTATION_DESCRIPTION_LEVELS);
  }

  private List<DescriptionLevel> splitChildLevelsAsObjects(String childLevels) {
    List<DescriptionLevel> res = new ArrayList<DescriptionLevel>();
    for (String level : splitList(childLevels)) {
      res.add(new DescriptionLevel(level));
    }
    return res;
  }

  private List<String> splitList(String list) {
    List<String> res = new ArrayList<String>();
    if (!(list == null || "".equals(list.trim()))) {
      String[] split = list.trim().split(",");
      if (split.length > 0) {
        for (String s : split) {
          if (!res.contains(s.trim())) {
            res.add(s.trim());
          }
        }
      }
    }
    return res;
  }
}
