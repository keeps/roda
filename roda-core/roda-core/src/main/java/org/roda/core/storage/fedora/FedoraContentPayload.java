/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.roda.core.storage.ContentPayload;

public class FedoraContentPayload implements ContentPayload {

  private final FedoraDatastream fds;

  public FedoraContentPayload(FedoraDatastream fds) {
    this.fds = fds;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    try {
      return fds.getContent();
    } catch (FedoraException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    InputStream inputStream = createInputStream();
    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(inputStream);

  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException("URI not supported for Fedora Datastreams");
  }

}
