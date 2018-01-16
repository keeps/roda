/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ReturnWithExceptions<T, S> {
  private List<Exception> exceptions;
  private T returnedObject;
  private S source;

  public ReturnWithExceptions() {
    setExceptions(new ArrayList<Exception>());
    setReturnedObject(null);
    setSource(null);
  }

  public ReturnWithExceptions(S source) {
    setExceptions(new ArrayList<Exception>());
    setReturnedObject(null);
    setSource(source);
  }

  public ReturnWithExceptions(List<Exception> errors, T ret, S source) {
    this.setExceptions(errors);
    this.setReturnedObject(ret);
    this.setSource(source);
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }

  public void setExceptions(List<Exception> exceptions) {
    this.exceptions = exceptions;
  }

  public void add(Exception exception) {
    this.exceptions.add(exception);
  }

  public void add(List<Exception> exceptions) {
    this.exceptions.addAll(exceptions);
  }

  public void add(ReturnWithExceptions<?, S> ts) {
    add(ts.getExceptions());
  }

  public void addTo(ReturnWithExceptions<?, S> ts) {
    ts.add(this);
  }

  public T getReturnedObject() {
    return returnedObject;
  }

  public void setReturnedObject(T ret) {
    this.returnedObject = ret;
  }

  public S getSource() {
    return source;
  }

  public void setSource(S source) {
    this.source = source;
  }

  public String printStackTraces() {
    String ret;
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); PrintWriter w = new PrintWriter(bout, true);) {
      w.println(String.format("%1$s source=%2$s", getClass().getSimpleName(), getSource().getClass().getSimpleName()));
      exceptions.forEach(e -> {
        w.println(String.format("[%1$s] %2$s", e.getClass().getName(), e.getMessage()));
        e.printStackTrace(w);
      });
      w.flush();
      ret = new String(bout.toByteArray());
    } catch (IOException e1) {
      ret = null;
    }
    return ret;
  }

  @Override
  public String toString() {
    return "ReturnWithExceptions [exceptions=" + exceptions + ", ret=" + returnedObject + ", source=" + source + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((exceptions == null) ? 0 : exceptions.hashCode());
    result = prime * result + ((returnedObject == null) ? 0 : returnedObject.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
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
    @SuppressWarnings("rawtypes")
    ReturnWithExceptions other = (ReturnWithExceptions) obj;
    if (exceptions == null) {
      if (other.exceptions != null)
        return false;
    } else if (!exceptions.equals(other.exceptions))
      return false;
    if (returnedObject == null) {
      if (other.returnedObject != null)
        return false;
    } else if (!returnedObject.equals(other.returnedObject))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    return true;
  }

}
