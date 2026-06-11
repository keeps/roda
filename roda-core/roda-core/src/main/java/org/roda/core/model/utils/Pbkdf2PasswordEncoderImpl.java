/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class Pbkdf2PasswordEncoderImpl implements PasswordEncoder {

  static final int hashBitSize = 512;
  static final int saltByteSize = 16;
  static final int pbkdf2Iterations = 10000;
  static final String algorithm = "PBKDF2WithHmacSHA512";
  final String prefix = "{PBKDF2-SHA512}";

  public Pbkdf2PasswordEncoderImpl() {
    // No-op: custom implementation
  }

  @Override
  public String encode(CharSequence rawPassword) {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[saltByteSize];
    random.nextBytes(salt);

    try {
      PBEKeySpec spec = new PBEKeySpec(rawPassword.toString().toCharArray(), salt, pbkdf2Iterations, hashBitSize);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
      byte[] hash = skf.generateSecret(spec).getEncoded();

      String salt64 = Base64.getEncoder().encodeToString(salt).replace("+", ".").replace("=", "");
      String hash64 = Base64.getEncoder().encodeToString(hash).replace("+", ".").replace("=", "");

      return prefix + pbkdf2Iterations + "$" + salt64 + "$" + hash64;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Could not create hash", e);
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    String[] parts = encodedPassword.split("\\$");
    if (parts.length != 3) {
      return false;
    }

    int iterations = Integer.parseInt(parts[0].substring(prefix.length()));
    byte[] salt = Base64.getDecoder().decode(parts[1].replace(".", "+") + "==");
    byte[] storedHash = Base64.getDecoder().decode(parts[2].replace(".", "+") + "==");

    try {
      PBEKeySpec spec = new PBEKeySpec(rawPassword.toString().toCharArray(), salt, iterations, hashBitSize);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
      byte[] computedHash = skf.generateSecret(spec).getEncoded();

      return MessageDigest.isEqual(storedHash, computedHash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      return false;
    }
  }

}
