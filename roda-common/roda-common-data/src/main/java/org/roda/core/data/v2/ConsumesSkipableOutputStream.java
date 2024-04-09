/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public interface ConsumesSkipableOutputStream extends ConsumesOutputStream {

  void consumeOutputStream(OutputStream output, int from, int len) throws IOException;

  void consumeOutputStream(OutputStream output, long from, long len) throws IOException, AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

}
