package org.roda.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

/**
 * Interface to keep information on how to access the contents of a binary file.
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public interface ContentPayload {

  /**
   * Create a new inputstream that should be explicitly closed after being
   * consumed.
   * 
   * @return
   */
  public InputStream createInputStream() throws IOException;

  /**
   * Write the current stream to the specified file path.
   * 
   * @param path
   * @throws IOException
   */
  public void writeToPath(Path path) throws IOException;

  /**
   * Retrieves an URI for the file (e.g. 'file:', 'http:', etc.)
   * 
   * @return
   * */
  public URI getURI() throws IOException, UnsupportedOperationException;
}
