package org.roda.core.storage.protocol;

import java.io.InputStream;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ProtocolManager {
  InputStream getInputStream();
  Boolean isAvailable();
}
