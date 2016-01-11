/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.common.InvalidDescriptionLevel;

/**
 * This is information about the description level of a
 * {@link SimpleDescriptionObject}.
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DescriptionLevelInfo implements EadCValue, Serializable {

  private static final long serialVersionUID = -5841187419574977416L;

  // description level
  private String level = null;
  private DescriptionLevelCategory category;
  private Map<String, String> labels;

  /**
   * Constructs an empty (<strong>invalid</strong>) {@link DescriptionLevelInfo}
   * .
   * <p>
   * <strong>This method should not be used. All the possible values for a
   * {@link DescriptionLevelInfo} are already defined as constant
   * values.</strong>
   * </p>
   */
  public DescriptionLevelInfo() {
  }

  public DescriptionLevelInfo(String level, DescriptionLevelCategory category, Map<String, String> labels) {
    super();
    this.level = level;
    this.category = category;
    this.labels = labels;
  }

  /**
   * @return the level
   */
  public String getLevel() {
    return level;
  }

  /**
   * If the level {@link String} for some reason a special word in some context
   * (e.g. the word "class" it's exclusive for the Java language and therefore
   * one can't use it as an assessor keyword)
   */
  public String getLevelSanitized() {
    if (level.equals("class")) {
      return "class_";
    } else {
      return level;
    }
  }

  /**
   * Sets the level (it gets trimmed in the process)
   * 
   * @param level
   *          the level to set.
   * @throws InvalidDescriptionLevel
   *           if the specified level is null or empty {@link String}.
   */
  public void setLevel(String level) throws InvalidDescriptionLevel {
    if (level != null && !"".equals(level.trim().toLowerCase())) {
      this.level = level.trim().toLowerCase();
    } else {
      throw new InvalidDescriptionLevel("Invalid level: " + level);
    }
  }

  public DescriptionLevel getDescriptionLevel() {
    return new DescriptionLevel(getLevel());
  }

  public DescriptionLevelCategory getCategory() {
    return category;
  }

  public void setCategory(DescriptionLevelCategory category) {
    this.category = category;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public void setLabel(String locale, String label) {
    if (labels == null) {
      labels = new HashMap<String, String>();
    }
    labels.put(locale, label);
  }

  public String getLabel(String locale) {
    String label = null;
    if (labels != null) {
      label = labels.get(locale);
    }
    return label;
  }

  @Override
  public String toString() {
    return "DescriptionLevelInfo [level=" + level + ", category=" + category + ", labels=" + labels + "]";
  }

}
