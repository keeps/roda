/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.roda.core.storage.ContentPayload;

/**
 * Class that implements {@code ContentPayload} for File System
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public class FSPathContentPayload implements ContentPayload {

  private final Path path;

  public FSPathContentPayload(Path path) {
    this.path = path;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return Files.newInputStream(path);
  }

  @Override
  public void writeToPath(Path outPath) throws IOException {
    Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    return path.toUri();
  }

}
