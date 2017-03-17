/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.descriptionlevels;

import java.io.Serializable;

/**
 * 
 * 
 * @author Rui Castro
 * @author Hélder Silva
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DescriptionLevel implements Serializable {
  private static final long serialVersionUID = 9038357012292858570L;

  // description level
  private String level = null;
  private String label;
  private String iconClass;

  public DescriptionLevel() {
    super();
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getIconClass() {
    return iconClass;
  }

  public void setIconClass(String iconClass) {
    this.iconClass = iconClass;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iconClass == null) ? 0 : iconClass.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((level == null) ? 0 : level.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DescriptionLevel other = (DescriptionLevel) obj;
    if (iconClass == null) {
      if (other.iconClass != null)
        return false;
    } else if (!iconClass.equals(other.iconClass))
      return false;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    if (level == null) {
      if (other.level != null)
        return false;
    } else if (!level.equals(other.level))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DescriptionLevel [level=" + level + ", label=" + label + ", iconClass=" + iconClass + "]";
  }

}