/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.common.certification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * Utility class to help create and validate digital signatures
 * 
 * @author Luis Faria
 * 
 */
public class SignatureUtility {

  private static final String SIGN_ALGORITHM = "SHA256WITHRSA";
  private static final String SIGN_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

  private final KeyStore ks;
  private final CMSSignedDataGenerator signGenerator;

  /**
   * Create a new signature utility
   * 
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   */
  public SignatureUtility() throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
    this(SIGN_PROVIDER);
  }

  protected SignatureUtility(String provider)
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
    this.ks = KeyStore.getInstance(KeyStore.getDefaultType());
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
   * @throws OperatorCreationException
   * @throws CertificateEncodingException
   * @throws CMSException
   */
  public void initSign(String alias, char[] password) throws UnrecoverableKeyException, KeyStoreException,
    NoSuchAlgorithmException, CertificateEncodingException, OperatorCreationException, CMSException {
    PrivateKey pk = (PrivateKey) ks.getKey(alias, password);
    Certificate[] certificateChain = ks.getCertificateChain(alias);
    if (certificateChain != null) {
      X509Certificate certificate = (X509Certificate) certificateChain[0];
      List<Certificate> certList = new ArrayList<Certificate>(Arrays.asList(certificateChain));

      signGenerator.addSignerInfoGenerator(
        new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider(SIGN_PROVIDER).build())
          .build(new JcaContentSignerBuilder(SIGN_ALGORITHM).setProvider(SIGN_PROVIDER).build(pk), certificate));

      JcaCertStore certs = new JcaCertStore(certList);
      signGenerator.addCertificates(certs);

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
  public byte[] sign(File file) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, CMSException {
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
  public void sign(File file, File signature)
    throws IOException, NoSuchAlgorithmException, NoSuchProviderException, CMSException {
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
   * @throws OperatorCreationException
   */
  public boolean verify(File file, File signature)
    throws NoSuchAlgorithmException, NoSuchProviderException, CertStoreException, CMSException, FileNotFoundException,
    IOException, CertificateException, OperatorCreationException {
    CMSProcessableFile cmsFile = new CMSProcessableFile(file);
    CMSSignedData signedData = new CMSSignedData(cmsFile, new FileInputStream(signature));

    return verifySignatures(signedData, null);
  }

  @SuppressWarnings("unchecked")
  private boolean verifySignatures(CMSSignedData s, byte[] contentDigest) throws NoSuchAlgorithmException,
    NoSuchProviderException, CMSException, CertStoreException, CertificateException, OperatorCreationException {
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

      SignerInformationVerifier signerVerifierInformation = new BcRSASignerInfoVerifierBuilder(
        new DefaultCMSSignatureAlgorithmNameGenerator(), new DefaultSignatureAlgorithmIdentifierFinder(),
        new DefaultDigestAlgorithmIdentifierFinder(), new BcDigestCalculatorProvider()).build(certHolder);
      boolean certValid = signer.verify(signerVerifierInformation);

      valid &= certValid;

      if (!certValid) {
        System.err.println("Invalid certificate " + certHolder);
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
