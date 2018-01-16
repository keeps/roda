/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class MultipleGenericException extends GenericException {
  private static final long serialVersionUID = 3865896551745064851L;
  private List<Exception> causes = new ArrayList<>();

  public MultipleGenericException() {
    super();
  }

  public MultipleGenericException(String message) {
    super(message);
  }

  public MultipleGenericException(String message, List<Exception> causes) {
    super(message);
    this.causes = causes;
  }

  public MultipleGenericException(List<Exception> causes) {
    super();
    this.causes = causes;
  }

  public static MultipleGenericException build(List<ReturnWithExceptions<?, ?>> list) {
    List<Exception> causes = new ArrayList<>();

    for (ReturnWithExceptions<?, ?> item : list) {
      for (Exception e : item.getExceptions()) {
        causes.add(e);
      }
    }

    return new MultipleGenericException(causes);
  }

  public List<Exception> getCauses() {
    return causes;
  }

  public void setCauses(List<Exception> causes) {
    this.causes = causes;
  }

}
