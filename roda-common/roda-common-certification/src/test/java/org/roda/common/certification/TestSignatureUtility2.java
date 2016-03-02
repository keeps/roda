/**
 * 
 */
package org.roda.common.certification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;

import org.bouncycastle.cms.CMSException;

/**
 * @author Luis Faria
 * 
 */
public class TestSignatureUtility2 {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length == 4) {
      File keystore = new File(args[0]);
      String privateKeyAlias = args[1];
      char[] password = args[2].toCharArray();
      File file = new File(args[3]);
      if (keystore.exists() && file.exists()) {
        try {
          System.out.println("Creating utility");
          SignatureUtility signatureUtility = new SignatureUtility();
          System.out.println("Loading key store " + keystore.getPath());
          signatureUtility.loadKeyStore(new FileInputStream(keystore), password);
          System.out.println("Initializing signature generator with private key " + privateKeyAlias);
          signatureUtility.initSign(privateKeyAlias, password);
          File signature = new File(file.getAbsolutePath() + ".detached.cms");
          System.out.println("Creating detached signature in " + signature.getPath());
          signatureUtility.sign(file, signature);
          System.out.println("Verifying signature");
          boolean verify = signatureUtility.verify(file, signature);
          if (verify) {
            System.out.println("Signature is valid");
          } else {
            System.err.println("Signature is NOT valid");
          }

        } catch (KeyStoreException e) {
          e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
        } catch (NoSuchProviderException e) {
          e.printStackTrace();
        } catch (CertificateException e) {
          e.printStackTrace();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
          e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
          e.printStackTrace();
        } catch (CertStoreException e) {
          e.printStackTrace();
        } catch (CMSException e) {
          e.printStackTrace();
        }
      } else {
        System.err.println("keystore and/or file do not exist");
        help();
      }
    } else {
      help();
    }

  }

  private static void help() {
    System.err.println("Syntax: java TestSignatureUtility2 keystore privateKeyAlias password file");
  }

}
