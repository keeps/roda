/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import org.roda.core.data.exceptions.GenericException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CryptographyUtils {

  private CryptographyUtils() {
    // do nothing
  }

  public static String hashTokenSHA256(String token) throws GenericException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

      // Convert the byte array into a hex string for easy storage
      StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
      for (byte hash : encodedHash) {
        String hex = Integer.toHexString(0xff & hash);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      // Wrap and throw using your application's exception handling
      throw new GenericException("Error initializing SHA-256 hashing algorithm", e);
    }
  }
}
