/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.adapter.filter;

/**
 * @author Rui Castro
 */
public class RegexFilterParameter extends FilterParameter {
  private static final long serialVersionUID = 6364739391605822125L;

  private String regex;

  /**
   * Constructs an empty {@link RegexFilterParameter}.
   */
  public RegexFilterParameter() {
  }

  /**
   * Constructs a {@link RegexFilterParameter} cloning an existing
   * {@link RegexFilterParameter}.
   * 
   * @param regexFilterParameter
   *          the {@link RegexFilterParameter} to clone.
   */
  public RegexFilterParameter(RegexFilterParameter regexFilterParameter) {
    this(regexFilterParameter.getName(), regexFilterParameter.getRegex());
  }

  /**
   * Constructs a {@link RegexFilterParameter} with the given parameters.
   * 
   * @param name
   * @param regex
   */
  public RegexFilterParameter(String name, String regex) {
    setName(name);
    setRegex(regex);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RegexFilterParameter(name=" + getName() + ", regex=" + getRegex() + ")";
  }

  /**
   * @see FilterParameter#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof RegexFilterParameter) {
      RegexFilterParameter other = (RegexFilterParameter) obj;
      equal = equal && super.equals(other);
      equal = equal && (getRegex() == other.getRegex() || getRegex().equals(other.getRegex()));
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @return the regex
   */
  public String getRegex() {
    return regex;
  }

  /**
   * @param regex
   *          the regex to set
   */
  public void setRegex(String regex) {
    this.regex = regex;
  }

}
