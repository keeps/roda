/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author António Lindo <alindo@keep.pt>
 *
 **/

public class SecureString implements CharSequence, Closeable, Serializable {
  private static final long serialVersionUID = -2870985634520825237L;
  private char[] chars;

  public SecureString() {
  }

  /**
   * Constructs a new SecureString which controls the passed in char array.
   *
   * Note: When this instance is closed, the array will be cleared.
   */

  public SecureString(char[] chars) {
    this.chars = Objects.requireNonNull(chars);
  }
  
  @Override
  public synchronized boolean equals(Object o) {
    ensureNotClosed();
    if (this == o)
      return true;
    if (o == null || o instanceof CharSequence == false)
      return false;
    CharSequence that = (CharSequence) o;
    if (chars.length != that.length()) {
      return false;
    }

    int equals = 0;
    for (int i = 0; i < chars.length; i++) {
      equals |= chars[i] ^ that.charAt(i);
    }

    return equals == 0;
  }

  @Override
  public synchronized int hashCode() {
    return Arrays.hashCode(chars);
  }

  @Override
  public synchronized int length() {
    ensureNotClosed();
    return chars.length;
  }

  @Override
  public synchronized char charAt(int index) {
    ensureNotClosed();
    return chars[index];
  }

  @Override
  public SecureString subSequence(int start, int end) {
    throw new UnsupportedOperationException("Cannot get subsequence of SecureString");
  }

  /**
   * Converts chars to a {@link String}. Avoid using this method as it creates a string with
   * the secure string content.
   */
  @Override
  public synchronized String toString() {
    return new String(chars);
  }

  /**
   * Closes the secure string by clearing the char array.
   */
  @Override
  public synchronized void close() {
    if (chars != null) {
      Arrays.fill(chars, '\0');
      chars = null;
    }
  }

  /**
   * Returns the char[].
   */
  public synchronized char[] getChars() {
    ensureNotClosed();
    return chars;
  }

  /**
   * Throws an exception if the secure string has been closed, which means that something is
   * trying to access the data after being closed.
   */
  private void ensureNotClosed() {
    if (chars == null) {
      throw new IllegalStateException("SecureString has already been closed");
    }
  }
}
