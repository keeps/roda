package org.roda.core.storage.protocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.data.utils.URLUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FileProtocolManager implements ProtocolManager {
  private final URI connectionString;
  private final Path path;

  public FileProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
    this.path = Paths.get(URLUtils.decode(connectionString.getPath()));
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(path.toFile());
  }

  @Override
  public Boolean isAvailable() {
    return Files.exists(path);
  }

  @Override
  public Long getSize() throws IOException {
    return Files.size(path);
  }

  @Override
  public void downloadResource(Path target) throws IOException {
    Path output = target.resolve(FilenameUtils.getName(path.toString()));
    Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
  }
}
