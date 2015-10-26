/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Rui Castro
 * 
 */
public class LangmaterialLanguages implements EadCValue, Serializable {
  private static final long serialVersionUID = 427437307263580922L;

  private String[] langmaterialLanguages = null;

  /**
   * Constructs a new empty {@link LangmaterialLanguages}.
   */
  public LangmaterialLanguages() {
  }

  /**
   * Constructs a new {@link LangmaterialLanguages} clonning an existing
   * {@link LangmaterialLanguages}.
   * 
   * @param langmaterialLanguages
   */
  public LangmaterialLanguages(LangmaterialLanguages langmaterialLanguages) {
    this(langmaterialLanguages.getLangmaterialLanguages());
  }

  /**
   * Constructs a new {@link LangmaterialLanguages} with the given
   * <code>langmaterialLanguages</code>.
   * 
   * @param langmaterialLanguages
   */
  public LangmaterialLanguages(String[] langmaterialLanguages) {
    setLangmaterialLanguages(langmaterialLanguages);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof LangmaterialLanguages) {
      LangmaterialLanguages other = (LangmaterialLanguages) obj;
      // return equals(this.langmaterialLanguages,
      // other.langmaterialLanguages);
      return Arrays.asList(this.langmaterialLanguages).equals(Arrays.asList(other.langmaterialLanguages));
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    String[] langmaterialLanguages2 = getLangmaterialLanguages();
    if (langmaterialLanguages2 == null) {
      langmaterialLanguages2 = new String[0];
    }

    return "LangmaterialLanguages( " + Arrays.asList(langmaterialLanguages2) + ")";
  }

  /**
   * @return the langmaterialLanguages
   */
  public String[] getLangmaterialLanguages() {
    return langmaterialLanguages;
  }

  /**
   * @param langmaterialLanguages
   *          the langmaterialLanguages to set
   */
  public void setLangmaterialLanguages(String[] langmaterialLanguages) {
    this.langmaterialLanguages = langmaterialLanguages;
  }
}
