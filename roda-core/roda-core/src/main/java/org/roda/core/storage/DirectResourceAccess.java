package org.roda.core.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

public interface DirectResourceAccess extends Closeable {

  Path getPath() throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

}
