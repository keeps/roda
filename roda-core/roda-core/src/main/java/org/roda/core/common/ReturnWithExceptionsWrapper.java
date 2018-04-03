/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.MultipleGenericException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnWithExceptionsWrapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReturnWithExceptionsWrapper.class);

  private List<ReturnWithExceptions<?, ?>> list;

  public ReturnWithExceptionsWrapper() {
    this.setList(new ArrayList<>());
  }

  public ReturnWithExceptionsWrapper(List<ReturnWithExceptions<?, ?>> list) {
    this.setList(list);
  }

  public List<ReturnWithExceptions<?, ?>> getList() {
    return list;
  }

  public void setList(List<ReturnWithExceptions<?, ?>> list) {
    this.list = list;
  }

  public void addToList(ReturnWithExceptions<?, ?> item) {
    if (item != null) {
      this.list.add(item);
    }
  }

  public String printStackTraces() {
    StringBuilder b = new StringBuilder();
    list.forEach(e -> b.append(e.printStackTraces()));
    return b.toString();
  }

  public boolean hasNoExceptions() {
    boolean isEmpty = true;
    for (ReturnWithExceptions<?, ?> item : list) {
      isEmpty = isEmpty && item.getExceptions().isEmpty();
    }
    return isEmpty;
  }

  public void failOnError() throws GenericException {
    if (!hasNoExceptions()) {
      if (list.size() == 1 && list.get(0).getExceptions().size() == 1) {
        // if there is one and only one exception throw it
        throw new GenericException(list.get(0).getExceptions().get(0));
      } else {
        LOGGER.error(printStackTraces());
        throw MultipleGenericException.build(list);
      }
    }
  }
}
