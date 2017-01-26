/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

/**
 * Utility class to help create and validate digital signatures
 * 
 * @author Luis Faria
 * 
 */
public class SignatureUtility {

  private static final String DEFAULT_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

  private final KeyStore ks;
  private final String provider;
  private final CMSSignedDataGenerator signGenerator;

  /**
   * Create a new signature utility
   * 
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   */
  public SignatureUtility() throws KeyStoreException {
    this(DEFAULT_PROVIDER);
  }

  protected SignatureUtility(String provider) throws KeyStoreException {
    this.ks = KeyStore.getInstance(KeyStore.getDefaultType());
    this.provider = provider;
    Security.addProvider(new BouncyCastleProvider());
    this.signGenerator = new CMSSignedDataGenerator();
  }

  /**
   * Load a new keystore
   * 
   * @param keystore
   *          input stream to the keystore
   * @param password
   *          keystore password
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   * @throws IOException
   */
  public void loadKeyStore(InputStream keystore, char[] password)
    throws NoSuchAlgorithmException, CertificateException, IOException {
    ks.load(keystore, password);
  }

  /**
   * Initialize parameters for signing
   * 
   * @param alias
   *          private key alias
   * 
   * @param password
   *          the keystore password
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws UnrecoverableKeyException
   * @throws CMSException
   * @throws NoSuchProviderException
   * @throws InvalidAlgorithmParameterException
   * @throws CertStoreException
   */
  public void initSign(String alias, char[] password)
    throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
    NoSuchProviderException, CertStoreException, CMSException {
    java.security.cert.Certificate[] certificateChain = ks.getCertificateChain(alias);
    if (certificateChain != null) {
      List<java.security.cert.Certificate> certList = new ArrayList<java.security.cert.Certificate>(
        Arrays.asList(certificateChain));

      CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), provider);
      signGenerator.addCertificatesAndCRLs(certs);
    } else {
      System.err.println("Certificate chain for " + alias + " not found");
    }
  }

  /**
   * Sign the file
   * 
   * @param file
   * 
   * @return an array of bytes with the signature
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws CMSException
   */
  public byte[] sign(File file) throws CMSException, IOException {
    CMSProcessableFile cmsFile = new CMSProcessableFile(file);
    CMSSignedData data = signGenerator.generate(cmsFile);
    return data.getEncoded();
  }

  /**
   * Sign the file
   * 
   * @param file
   *          the file to sign
   * @param signature
   *          the file where to save the signature. If the file exists it will
   *          be overridden
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws CMSException
   */
  public void sign(File file, File signature) throws IOException, CMSException {
    if (signature.exists()) {
      signature.delete();
    }
    signature.createNewFile();
    FileOutputStream out = new FileOutputStream(signature);
    byte[] signatureArray = sign(file);
    out.write(signatureArray);
    out.close();

  }

  /**
   * Verify detached signature
   * 
   * @param file
   * @param signature
   * @return true if valid
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws CertStoreException
   * @throws CMSException
   * @throws FileNotFoundException
   * @throws IOException
   * @throws CertificateException
   */
  public boolean verify(File file, File signature) throws FileNotFoundException, CMSException, CertificateException,
    NoSuchAlgorithmException, NoSuchProviderException {
    CMSProcessableFile cmsFile = new CMSProcessableFile(file);
    CMSSignedData signedData = new CMSSignedData(cmsFile, new FileInputStream(signature));
    return verifySignatures(signedData, null);
  }

  private boolean verifySignatures(CMSSignedData s, byte[] contentDigest)
    throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException, CMSException {
    boolean valid = true;

    // CertStore certStore = s.getCertificatesAndCRLs("Collection", provider);
    Store<?> certStore = s.getCertificates();
    SignerInformationStore signers = s.getSignerInfos();

    Collection<SignerInformation> c = signers.getSigners();
    Iterator<SignerInformation> it = c.iterator();

    while (it.hasNext()) {
      SignerInformation signer = it.next();
      Collection<?> certCollection = certStore.getMatches(signer.getSID());

      Iterator<?> certIt = certCollection.iterator();
      X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
      X509Certificate cert = new JcaX509CertificateConverter().setProvider(provider).getCertificate(certHolder);
      boolean certValid = signer.verify(cert, provider);
      valid &= certValid;

      if (!certValid) {
        System.err.println("Invalid certificate " + cert);
      }

      if (contentDigest != null) {
        boolean digestValid = MessageDigest.isEqual(contentDigest, signer.getContentDigest());
        valid &= digestValid;

        if (!digestValid) {
          System.err.println("Invalid digest " + contentDigest);
        }
      }
    }

    return valid;
  }

}
