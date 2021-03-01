package org.roda.core.storage.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.ShallowFiles;
import org.roda.core.storage.ContentPayload;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShallowFileContentPayload implements ContentPayload {
  private Path contentPath;
  private ShallowFiles shallowFiles;

  public ShallowFileContentPayload(ShallowFiles shallowFiles) {
    this.shallowFiles = shallowFiles;
  }

  public ShallowFiles getShallowFiles() {
    return shallowFiles;
  }

  public void addShallowFiles(ShallowFiles shallowFiles) {
    for (ShallowFile object : shallowFiles.getObjects()) {
      this.shallowFiles.addObject(object);
    }
  }

  @Override
  public InputStream createInputStream() throws IOException {
    String content = "";
    for (ShallowFile file : this.shallowFiles.getObjects()) {
      content = content.concat(JsonUtils.getJsonFromObject(file) + "\n");
    }
    return new ByteArrayInputStream(content.getBytes());
  }

  @Override
  public void writeToPath(Path path) throws IOException {
    try (InputStream inputStream = createInputStream()) {
      Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Override
  public URI getURI() throws IOException, UnsupportedOperationException {
    if (contentPath == null) {
      contentPath = Files.createTempFile("content", ".jsonl");
      writeToPath(contentPath);
    }
    return contentPath.toUri();
  }
}
