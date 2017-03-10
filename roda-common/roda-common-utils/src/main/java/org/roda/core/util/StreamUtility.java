/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rui Castro
 */
public class StreamUtility {

  /**
   * Class that ensure that the underlying input stream is always available (it
   * will not be closed after the first use)
   */
  public static class UnclosableBufferedInputStream extends BufferedInputStream {

    public UnclosableBufferedInputStream(InputStream in) {
      super(in);
      super.mark(Integer.MAX_VALUE);
    }

    @Override
    public void close() throws IOException {
      super.reset();
    }
  }

  /**
   * Reads bytes from a {@link InputStream}.
   * 
   * @param in
   * 
   * @return an array of bytes with the contents of <code>is</code>.
   * 
   * @throws IOException
   */
  public static byte[] byteArrayFromInputStream(InputStream in) throws IOException {

    int bufferSize = 4096;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] b = new byte[bufferSize];
    for (int n; (n = in.read(b)) != -1;) {
      baos.write(b, 0, n);
    }

    return baos.toByteArray();
  }

  /**
   * Reads the contents of an {@link InputStream} to a {@link String}.
   * 
   * @param in
   *          the source {@link InputStream}.
   * 
   * @return a {@link String} with the contents of the {@link InputStream}.
   * 
   * @throws IOException
   */
  public static String inputStreamToString(InputStream in) throws IOException {
    StringBuilder out = new StringBuilder();
    byte[] b = new byte[4096];
    for (int n; (n = in.read(b)) != -1;) {
      out.append(new String(b, 0, n));
    }
    return out.toString();
  }

}
