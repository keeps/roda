package org.roda.core.data.v2.index;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;

public interface IndexRunnable<T extends IsIndexed> {

  public void run(T item) throws GenericException, RequestNotValidException, AuthorizationDeniedException;

}
