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

import org.apache.commons.io.IOUtils;

public class XMLContentPayload implements ContentPayload {
  String content;
  Path contentPath;

  public XMLContentPayload(String content) {
    this.content = content;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return new ByteArrayInputStream(content.getBytes());
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    InputStream inputStream = createInputStream();
    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(inputStream);
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    if (contentPath == null) {
      contentPath = Files.createTempFile("content", ".xml");
      writeToPath(contentPath);
    }
    return contentPath.toUri();
  }

}