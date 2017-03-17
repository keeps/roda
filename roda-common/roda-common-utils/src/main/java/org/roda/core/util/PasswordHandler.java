/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * <p>
 * See <a href=
 * "http://www.ldapguru.net/modules/newbb/viewtopic.php?topic_id=1479&forum=6" >
 * </a>
 * </p>
 * 
 * @author Rohan Pinto <rohan@rohanpinto.com>
 * @author Rui Castro
 */
public class PasswordHandler {

  private static PasswordHandler handler;

  protected PasswordHandler() {
    // do nothing
  }

  /**
   * @return an instance of PasswordHandler.
   */
  public static synchronized PasswordHandler getInstance() {
    if (handler == null) {
      handler = new PasswordHandler();
    }
    return handler;
  }

  /**
   * @param digest
   * @param password
   * @return a <code>true</code> if the <code>digest</code> and
   *         <code>password</code> match and <code>false</code> otherwise.
   * @throws NoSuchAlgorithmException
   */
  public boolean verify(String digest, String password) throws NoSuchAlgorithmException {

    String changedDigest = digest;
    String alg = null;
    int size = 0;

    if (changedDigest.regionMatches(true, 0, "{CRYPT}", 0, 7)) {
      changedDigest = changedDigest.substring(7);
      return UnixCrypt.matches(changedDigest, password);
    } else if (changedDigest.regionMatches(true, 0, "{SHA}", 0, 5)) {
      changedDigest = changedDigest.substring(5); // ignore the label
      alg = "SHA-1";
      size = 20;
    } else if (changedDigest.regionMatches(true, 0, "{SSHA}", 0, 6)) {
      changedDigest = changedDigest.substring(6); // ignore the label
      alg = "SHA-1";
      size = 20;
    } else if (changedDigest.regionMatches(true, 0, "{MD5}", 0, 5)) {
      changedDigest = changedDigest.substring(5); // ignore the label
      alg = "MD5";
      size = 16;
    } else if (changedDigest.regionMatches(true, 0, "{SMD5}", 0, 6)) {
      changedDigest = changedDigest.substring(6); // ignore the label
      alg = "MD5";
      size = 16;
    }

    MessageDigest msgDigest = MessageDigest.getInstance(alg);

    byte[][] hs = split(Base64.decode(changedDigest.toCharArray()), size);
    byte[] hash = hs[0];
    byte[] salt = hs[1];

    msgDigest.reset();
    msgDigest.update(password.getBytes());
    msgDigest.update(salt);

    byte[] pwhash = msgDigest.digest();
    return MessageDigest.isEqual(hash, pwhash);
  }

  /**
   * @param password
   * @param saltHex
   * @param algorithm
   * @return a {@link String} with the digest for given <code>password</code>,
   *         <code>saltHex</code> and <code>algorithm</code>.
   * @throws NoSuchAlgorithmException
   */
  public String generateDigest(String password, String saltHex, String algorithm) throws NoSuchAlgorithmException {
    String alg = algorithm;

    if ("crypt".equalsIgnoreCase(algorithm)) {
      return "{CRYPT}" + UnixCrypt.crypt(password);
    } else if ("sha".equalsIgnoreCase(algorithm)) {
      alg = "SHA-1";
    } else if ("md5".equalsIgnoreCase(algorithm)) {
      alg = "MD5";
    }

    MessageDigest msgDigest = MessageDigest.getInstance(alg);

    byte[] salt = {};
    if (saltHex != null) {
      salt = fromHex(saltHex);
    }

    String label = null;

    if (alg.startsWith("SHA")) {
      label = (salt.length > 0) ? "{SSHA}" : "{SHA}";
    } else if (alg.startsWith("MD5")) {
      label = (salt.length > 0) ? "{SMD5}" : "{MD5}";
    }

    msgDigest.reset();
    msgDigest.update(password.getBytes());
    msgDigest.update(salt);

    byte[] pwhash = msgDigest.digest();

    StringBuilder digest = new StringBuilder(label);
    digest.append(Base64.encode(concatenate(pwhash, salt)));
    return digest.toString();
  }

  private static byte[] concatenate(byte[] l, byte[] r) {
    byte[] b = new byte[l.length + r.length];
    System.arraycopy(l, 0, b, 0, l.length);
    System.arraycopy(r, 0, b, l.length, r.length);
    return b;
  }

  private static byte[][] split(byte[] src, int n) {
    byte[] l;
    byte[] r;
    if (src.length <= n) {
      l = src;
      r = new byte[0];
    } else {
      l = new byte[n];
      r = new byte[src.length - n];
      System.arraycopy(src, 0, l, 0, n);
      System.arraycopy(src, n, r, 0, r.length);
    }
    byte[][] lr = {l, r};
    return lr;
  }

  private static String hexits = "0123456789abcdef";

  @SuppressWarnings("unused")
  private static String toHex(byte[] block) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < block.length; ++i) {
      buf.append(hexits.charAt((block[i] >>> 4) & 0xf));
      buf.append(hexits.charAt(block[i] & 0xf));
    }
    return buf + "";
  }

  private static byte[] fromHex(String s) {
    String ls = s.toLowerCase();
    byte[] b = new byte[(ls.length() + 1) / 2];
    int j = 0;
    int h;
    int nybble = -1;
    for (int i = 0; i < ls.length(); ++i) {
      h = hexits.indexOf(ls.charAt(i));
      if (h >= 0) {
        if (nybble < 0) {
          nybble = h;
        } else {
          b[j++] = (byte) ((nybble << 4) + h);
          nybble = -1;
        }
      }
    }
    if (nybble >= 0) {
      b[j++] = (byte) (nybble << 4);
    }
    if (j < b.length) {
      byte[] b2 = new byte[j];
      System.arraycopy(b, 0, b2, 0, j);
      b = b2;
    }
    return b;
  }

  public static String generateRandomPassword(int length) {
    StringBuilder buffer = new StringBuilder();
    Random random = new Random();
    char[] chars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
      's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
      'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    for (int i = 0; i < length; i++) {
      buffer.append(chars[random.nextInt(chars.length)]);
    }

    return buffer.toString();
  }

}
