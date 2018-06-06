/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.roda.core.storage.fs.FSUtils;

/**
 * @author lfaria
 *
 */
public class StringContentPayload implements ContentPayload {
  private String content;
  private Path contentPath;

  public StringContentPayload(String content) {
    this.content = content;
    contentPath = null;
  }

  @Override
  public InputStream createInputStream() {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    InputStream inputStream = createInputStream();
    FSUtils.safeUpdate(inputStream, path);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    if (contentPath == null) {
      contentPath = Files.createTempFile("test", ".tmp");
      writeToPath(contentPath);
    }
    return contentPath.toUri();
  }

  @Override
  public String toString() {
    return content;
  }
}
