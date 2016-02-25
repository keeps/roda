/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.io.Serializable;

public class Triple<A, B, C> implements Serializable {

  private static final long serialVersionUID = 2589199292145415369L;
  
  private A first;
  private B second;
  private C third;

  public static <A, B, C> Triple<A, B, C> create(A first, B second, C third) {
    return new Triple<A, B, C>(first, second, third);
  }

  public Triple() {
    super();
  }

  public Triple(A first, B second, C third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public A getFirst() {
    return first;
  }

  public void setFirst(A first) {
    this.first = first;
  }

  public B getSecond() {
    return second;
  }

  public void setSecond(B second) {
    this.second = second;
  }

  public C getThird() {
    return third;
  }

  public void setThird(C third) {
    this.third = third;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    result = prime * result + ((third == null) ? 0 : third.hashCode());
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
    Triple other = (Triple) obj;
    if (first == null) {
      if (other.first != null)
        return false;
    } else if (!first.equals(other.first))
      return false;
    if (second == null) {
      if (other.second != null)
        return false;
    } else if (!second.equals(other.second))
      return false;
    if (third == null) {
      if (other.third != null)
        return false;
    } else if (!third.equals(other.third))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Triple [first=" + first + ", second=" + second + ", third=" + third + "]";
  }

}
