package org.roda.core.common;

import java.util.ArrayList;
import java.util.List;

public class ReturnWithExceptions<T> {
  private List<Exception> exceptions;
  private T ret;

  public ReturnWithExceptions() {
    setExceptions(new ArrayList<Exception>());
    setRet(null);
  }

  public ReturnWithExceptions(List<Exception> errors, T ret) {
    this.setExceptions(errors);
    this.setRet(ret);
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }

  public void setExceptions(List<Exception> exceptions) {
    this.exceptions = exceptions;
  }

  public void addException(Exception exception) {
    this.exceptions.add(exception);
  }

  public void addExceptions(List<Exception> exceptions) {
    this.exceptions.addAll(exceptions);
  }

  public T getRet() {
    return ret;
  }

  public void setRet(T ret) {
    this.ret = ret;
  }

  @Override
  public String toString() {
    return "ReturnWithExceptions [exceptions=" + exceptions + ", ret=" + ret + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((exceptions == null) ? 0 : exceptions.hashCode());
    result = prime * result + ((ret == null) ? 0 : ret.hashCode());
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
    ReturnWithExceptions other = (ReturnWithExceptions) obj;
    if (exceptions == null) {
      if (other.exceptions != null)
        return false;
    } else if (!exceptions.equals(other.exceptions))
      return false;
    if (ret == null) {
      if (other.ret != null)
        return false;
    } else if (!ret.equals(other.ret))
      return false;
    return true;
  }

}
