/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public interface ConsumesOutputStream {
  void consumeOutputStream(OutputStream out) throws IOException;

  long getSize();

  Date getLastModified();

  String getFileName();

  String getMediaType();

}
