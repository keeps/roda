package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;

public interface ProvidesInputStream {
  public InputStream createInputStream() throws IOException;
}
