package org.roda.core.storage.protocol;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NFSProtocolManager implements ProtocolManager {
  private final URI connectionString;

  public NFSProtocolManager(URI connectionString) {
    this.connectionString = connectionString;
  }

  @Override
  public InputStream getInputStream() {
    return null;
  }

  @Override
  public Boolean isAvailable() {
    return null;
  }

  @Override
  public void downloadResource(Path target) {

  }
}
