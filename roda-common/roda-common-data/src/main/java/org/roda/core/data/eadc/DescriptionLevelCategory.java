package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class DescriptionLevelCategory implements EadCValue, Serializable {

  private static final long serialVersionUID = -3280565214967201182L;

  public static final DescriptionLevelCategory ORGANIZATIONAL = new DescriptionLevelCategory("organizational");

  public static final DescriptionLevelCategory CATEGORIZATION = new DescriptionLevelCategory("categorization");
  public static final DescriptionLevelCategory INTELLECTUAL_ENTITY = new DescriptionLevelCategory("intellectual_entity");
  public static final DescriptionLevelCategory INTELLECTUAL_SUBDIVISION = new DescriptionLevelCategory(
    "intellectual_subdivision");
  public static final DescriptionLevelCategory MECHANIC_SUBDIVISION = new DescriptionLevelCategory(
    "mechanic_subdivision");
  public static final DescriptionLevelCategory REPRESENTATIONAL = new DescriptionLevelCategory("representational");

  public static List<DescriptionLevelCategory> CATEGORIES = Arrays.asList(ORGANIZATIONAL, CATEGORIZATION,
    INTELLECTUAL_ENTITY, INTELLECTUAL_SUBDIVISION, MECHANIC_SUBDIVISION, REPRESENTATIONAL);

  private String category;

  public DescriptionLevelCategory() {
  }

  public DescriptionLevelCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
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
    DescriptionLevelCategory other = (DescriptionLevelCategory) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DescriptionLevelCategory [category=" + category + "]";
  }

  public static boolean isValid(String category) {
    boolean valid = false;
    if (category != null) {
      for (DescriptionLevelCategory cat : CATEGORIES) {
        if (cat.getCategory().equals(category)) {
          valid = true;
          break;
        }
      }
    }
    return valid;
  }

  public static DescriptionLevelCategory valueOf(String category) {
    DescriptionLevelCategory ret;
    if (isValid(category)) {
      ret = new DescriptionLevelCategory(category);
    } else {
      throw new IllegalArgumentException(category);
    }
    return ret;
  }

}
