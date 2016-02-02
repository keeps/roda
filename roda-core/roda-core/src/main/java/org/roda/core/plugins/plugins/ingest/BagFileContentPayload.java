package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    Files.copy(createInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
