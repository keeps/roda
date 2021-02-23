package org.roda.core.storage.protocol;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FileProtocolManager implements ProtocolManager{
  URI connectionString;

  public FileProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
  }

  @Override
  public InputStream getInputStream() {
    try {
      return connectionString.toURL().openStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Boolean isAvailable() {
    return true;
  }

  @Override
  public void downloadResource(Path target) {
    Path output = target.resolve(FilenameUtils.getName(connectionString.getPath()));
    Path input = Paths.get(connectionString.getPath());
    try {
      Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
