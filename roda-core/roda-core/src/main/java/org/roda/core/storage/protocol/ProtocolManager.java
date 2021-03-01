package org.roda.core.storage.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ProtocolManager {

  InputStream getInputStream() throws IOException;

  Boolean isAvailable();

  Long getSize() throws IOException;

  void downloadResource(Path target) throws IOException;
}
