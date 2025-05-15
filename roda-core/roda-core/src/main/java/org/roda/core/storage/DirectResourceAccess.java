/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.Closeable;
import java.nio.file.Path;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

public interface DirectResourceAccess extends Closeable {

  Path getPath() throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  boolean isDirectory();

  boolean exists();
}
