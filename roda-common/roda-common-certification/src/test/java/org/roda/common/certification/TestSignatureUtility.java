/**
 * 
 */
package org.roda.common.certification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import org.bouncycastle.cms.CMSException;

/**
 * @author Luis Faria
 * 
 */
public class TestSignatureUtility {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length == 2) {
      File file = new File(args[0]);
      File signature = new File(args[1]);
      if (file.exists() && signature.exists()) {
        try {
          System.out.println("Creating utility");
          SignatureUtility signatureUtility = new SignatureUtility();
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
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (CertificateExpiredException e) {
          e.printStackTrace();
        } catch (CertificateNotYetValidException e) {
          e.printStackTrace();
        } catch (CertStoreException e) {
          e.printStackTrace();
        } catch (CMSException e) {
          e.printStackTrace();
        } catch (CertificateException e) {
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
    System.err.println("Syntax: java TestSignatureUtility file signature");
  }

}
