/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.common.certification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestODFSigning {

  public static void main(String[] args) throws Exception {

    Security.addProvider(new BouncyCastleProvider());
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(new FileInputStream("/home/nvieira/.roda/config/certs/keystore.jks"), "changeit".toCharArray());
    X509Certificate certificate = (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
    Key key = keystore.getKey("certificate_alias", "changeit".toCharArray());

    ByteArrayInputStream bais = ODFSignatureUtils.createSignature("/home/nvieira/hello-world.odt", certificate, key);

    File file = new File("/home/nvieira/signed-hello-world.odt");

    if (file != null) {
      byte[] buffer = new byte[2048];
      int length = 0;
      FileOutputStream fos = new FileOutputStream(file);

      while ((length = bais.read(buffer)) >= 0) {
        fos.write(buffer, 0, length);
      }

      fos.close();
    }
  }

}