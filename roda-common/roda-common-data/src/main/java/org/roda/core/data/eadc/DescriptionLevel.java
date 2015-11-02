/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;

import org.roda.core.common.InvalidDescriptionLevel;
import org.roda.core.data.v2.SimpleDescriptionObject;

/**
 * This is the description level of a {@link SimpleDescriptionObject}.
 * 
 * @author Rui Castro
 * @author Hélder Silva
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DescriptionLevel implements EadCValue, Comparable<DescriptionLevel>, Serializable {
  private static final long serialVersionUID = 9038357012292858570L;

  // description level
  private String level = null;

  /**
   * Description Object Level Fonds (fonds)
   */
  // FIXME this should be removed in order to have a fully programmable
  // description levels system
  public static final DescriptionLevel FONDS = new DescriptionLevel("fonds");

  /**
   * Constructs an empty (<strong>invalid</strong>) {@link DescriptionLevel}.
   * <p>
   * <strong>This method should not be used. All the possible values for a
   * {@link DescriptionLevel} are already defined as constant values.</strong>
   * </p>
   */
  public DescriptionLevel() {
  }

  /**
   * Constructs a {@link DescriptionLevel} clonning an existing
   * {@link DescriptionLevel}.
   * 
   * @param dLevel
   *          the {@link DescriptionLevel} to clone.
   * 
   * @throws InvalidDescriptionLevel
   *           if the specified level is not one of the allowed levels.
   */
  public DescriptionLevel(DescriptionLevel dLevel) throws InvalidDescriptionLevel {
    this(dLevel.getLevel());
  }

  /**
   * Constructs a new {@link DescriptionLevel} of the specified level.
   * 
   * @param level
   *          the level of this {@link DescriptionLevel}.
   * 
   * @throws InvalidDescriptionLevel
   *           if the specified level is not one of the allowed levels.
   */
  public DescriptionLevel(String level) throws InvalidDescriptionLevel {
    setLevel(level);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = false;

    if (obj != null && obj instanceof DescriptionLevel) {
      DescriptionLevel other = (DescriptionLevel) obj;
      equal = getLevel() == other.getLevel() || getLevel().equals(other.getLevel());
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * Compare to other description level
   * 
   * @param other
   * @return (&lt;0), 0 or (&gt;0) if other description level is a descendant of
   *         this object, equal to this object or a ascendant level of this
   *         object.
   */
  @Deprecated
  public int compareTo(DescriptionLevel other) {

    // DescriptionLevel otherDescriptionLevel = (DescriptionLevel) other;
    //
    // List<DescriptionLevel> levels = DESCRIPTION_LEVELS;
    //
    // return levels.indexOf(this) - levels.indexOf(otherDescriptionLevel);
    return 0;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return getLevel();
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
}