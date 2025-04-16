/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.roda.core.common.ProvidesInputStream;
import org.roda.core.storage.fs.FSUtils;

public class InputStreamContentPayload implements ContentPayload {

  private final ProvidesInputStream inputStreamProvider;
  private URI uri = null;

  public InputStreamContentPayload(ProvidesInputStream inputStreamProvider) {
    super();
    this.inputStreamProvider = inputStreamProvider;
  }

  public InputStreamContentPayload(ProvidesInputStream inputStreamProvider, URI uri) {
    this(inputStreamProvider);
    this.uri = uri;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return inputStreamProvider.createInputStream();
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    try (InputStream inputStream = createInputStream()) {
      Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Override
  public URI getURI() throws UnsupportedOperationException {
    if (uri == null) {
      throw new UnsupportedOperationException();
    }
    return uri;
  }

}
