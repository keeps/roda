/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.roda.core.storage.ContentPayload;

import gov.loc.repository.bagit.BagFile;

public class BagFileContentPayload implements ContentPayload {

  private final BagFile bagFile;

  public BagFileContentPayload(BagFile bagFile) {
    super();
    this.bagFile = bagFile;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return bagFile.newInputStream();
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    InputStream inputStream = createInputStream();
    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(inputStream);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
