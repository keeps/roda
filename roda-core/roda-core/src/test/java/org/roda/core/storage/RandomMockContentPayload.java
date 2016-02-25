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
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomMockContentPayload implements ContentPayload {
  private String content;
  private Path contentPath;

  public RandomMockContentPayload() {
    content = RandomStringUtils.randomAlphanumeric(2000);
    contentPath = null;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    Files.copy(createInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    if (contentPath == null) {
      contentPath = Files.createTempFile("test", ".tmp");
      writeToPath(contentPath);
    }
    return contentPath.toUri();
  }

}
