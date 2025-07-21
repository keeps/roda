/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serial;

/**
 * @author Rui Castro
 */
@JsonTypeName("LikeFilterParameter")
public class LikeFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = 5402707366601834582L;

  private String expression;

  /**
   * Constructs an empty {@link LikeFilterParameter}.
   */
  public LikeFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link LikeFilterParameter} cloning an existing
   * {@link LikeFilterParameter}.
   *
   * @param likeFilterParameter
   *          the {@link LikeFilterParameter} to clone.
   */
  public LikeFilterParameter(LikeFilterParameter likeFilterParameter) {
    this(likeFilterParameter.getName(), likeFilterParameter.getExpression());
  }

  /**
   * Constructs a {@link LikeFilterParameter} with the given parameters.
   *
   * @param name
   * @param expression
   */
  public LikeFilterParameter(String name, String expression) {
    setName(name);
    setExpression(expression);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "LikeFilterParameter(name=" + getName() + ", expression=" + getExpression() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((expression == null) ? 0 : expression.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    boolean equal;

    if (obj != null && obj instanceof LikeFilterParameter) {
      LikeFilterParameter other = (LikeFilterParameter) obj;
      equal = super.equals(other);
      equal = equal && (getExpression() == other.getExpression() || getExpression().equals(other.getExpression()));
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  /**
   * @param expression
   *          the expression to set
   */
  public void setExpression(String expression) {
    this.expression = expression;
  }

}
