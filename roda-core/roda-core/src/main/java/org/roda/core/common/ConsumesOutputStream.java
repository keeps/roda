package org.roda.core.common;

import java.io.IOException;
import java.io.OutputStream;

public interface ConsumesOutputStream {
  void consumeOutputStream(OutputStream out) throws IOException;
  
  String getFileName();
  
  String getMediaType();
  
}
