package org.roda.core.storage.protocol;

import java.io.InputStream;
import java.net.URI;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NFSProtocolManager implements ProtocolManager {

  URI connectionString;
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
}
