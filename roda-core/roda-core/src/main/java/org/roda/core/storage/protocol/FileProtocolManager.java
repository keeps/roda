package org.roda.core.storage.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
}
