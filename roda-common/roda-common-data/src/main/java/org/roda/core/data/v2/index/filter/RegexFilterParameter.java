/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((regex == null) ? 0 : regex.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof RegexFilterParameter)) {
      return false;
    }
    RegexFilterParameter other = (RegexFilterParameter) obj;
    if (regex == null) {
      if (other.regex != null) {
        return false;
      }
    } else if (!regex.equals(other.regex)) {
      return false;
    }
    return true;
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
