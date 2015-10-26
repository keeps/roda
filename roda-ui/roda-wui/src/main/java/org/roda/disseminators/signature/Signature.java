/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.signature;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.roda.disseminators.RepresentationDownload.RepresentationDownload;
import org.roda.disseminators.common.RepresentationHelper;

/**
 * Servlet implementation class Signature
 */
public class Signature extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private Logger logger = Logger.getLogger(RepresentationDownload.class);
  // private SignatureUtility signatureUtility = null;
  private RepresentationHelper representationHelper = null;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public Signature() {
    super();
    // try {
    // signatureUtility = new SignatureUtility();
    // String keystorePath = RodaClientFactory.getRodaProperties()
    // .getProperty("roda.disseminators.signature.keystore.path");
    // String keystorePassword = RodaClientFactory.getRodaProperties()
    // .getProperty(
    // "roda.disseminators.signature.keystore.password");
    // String keystoreAlias = RodaClientFactory.getRodaProperties()
    // .getProperty("roda.disseminators.signature.keystore.alias");
    //
    // if (keystorePath != null) {
    // try {
    // signatureUtility.loadKeyStore(new FileInputStream(
    // keystorePath), keystorePassword.toCharArray());
    // } catch (CertificateException e) {
    // logger.error("Cannot load keystore " + keystorePath, e);
    // } catch (FileNotFoundException e) {
    // logger.error("Cannot load keystore " + keystorePath, e);
    // } catch (IOException e) {
    // logger.error("Cannot load keystore " + keystorePath, e);
    // }
    // }
    // signatureUtility.initSign(keystoreAlias, keystorePassword
    // .toCharArray());
    // } catch (KeyStoreException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (NoSuchAlgorithmException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (NoSuchProviderException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (UnrecoverableKeyException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (InvalidAlgorithmParameterException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (CertStoreException e) {
    // logger.error("Cannot create signature utility", e);
    // } catch (CMSException e) {
    // logger.error("Cannot create signature utility", e);
    // }
  }

  private RepresentationHelper getRepresentationHelper() throws IOException {
    if (representationHelper == null) {
      representationHelper = new RepresentationHelper();
    }
    return representationHelper;
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    int separatorIndex = pathInfo.indexOf('/', 2);
    separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo.length();
    String pid = pathInfo.substring(1, separatorIndex);
    logger.info("pid=" + pid);
    // try {
    // RepresentationObject rep = RodaClientFactory.getRodaClient(
    // request.getSession()).getBrowserService()
    // .getRepresentationObject(pid);
    //
    // if (pid != null) {
    // sendSignedRepresentation(request, rep, response);
    // } else {
    // logger.error("Request with no PID defined");
    // response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    // "parameter pid must be defined");
    // }
    //
    // } catch (LoginException e) {
    // logger.error("Login Failure", e);
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
    // .getMessage());
    // } catch (NoSuchRODAObjectException e) {
    // logger.error("Object does not exist", e);
    // response
    // .sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    // } catch (BrowserException e) {
    // logger.error("Browser Exception", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (RODAClientException e) {
    // logger.error("RODA Client Exception", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (NoSuchAlgorithmException e) {
    // logger.error("Signature Utility", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (NoSuchProviderException e) {
    // logger.error("Signature Utility", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (CMSException e) {
    // logger.error("Signature Utility", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }catch (RemoteException e) {
    // RODAException exception = RODAClient.parseRemoteException(e);
    // if (exception instanceof AuthorizationDeniedException) {
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
    // exception.getMessage());
    // } else {
    // logger.error("RODA Exception", e);
    // response.sendError(
    // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }
    //
    // }
  }

  // private void sendSignedRepresentation(HttpServletRequest request,
  // RepresentationObject rep, HttpServletResponse response)
  // throws LoginException, RODAClientException, HttpException,
  // IOException, NoSuchAlgorithmException, NoSuchProviderException,
  // CMSException {
  // // If representation consists of a single file, create a file with the
  // // signature and send a zip with both files to user
  // if (rep.getPartFiles().length == 0) {
  // // Get root file input stream
  // InputStream rootInputStream = getRepresentationHelper()
  // .getRootInputStream(request, rep);
  //
  // // Create temporary files
  // File rootTempFile = File.createTempFile("root", null);
  // File signatureTempFile = File.createTempFile("sign", null);
  //
  // // Copy root file into temporary root file
  // IOUtils.copy(rootInputStream, new FileOutputStream(rootTempFile));
  //
  // // Sign root file into temporary signature file
  // signatureUtility.sign(rootTempFile, signatureTempFile);
  //
  // // Add root file and signature to a list
  // List<ZipEntryInfo> files = new ArrayList<ZipEntryInfo>();
  // files.add(new ZipEntryInfo(rep.getRootFile().getOriginalName(),
  // rootTempFile));
  // files.add(new ZipEntryInfo(rep.getRootFile().getOriginalName()
  // + ".p7s", signatureTempFile));
  //
  // // Create and send the zipped list to user
  // response.setContentType("application/zip");
  // response.setHeader("Content-Disposition", "attachment; filename="
  // + rep.getLabel() + "-signed.zip");
  //
  // ZipTools.zip(files, response.getOutputStream());
  //
  // // Delete temporary files
  // rootTempFile.delete();
  // signatureTempFile.delete();
  //
  // }
  // // If representation consists of multiple files, create a zip with
  // // multiple files, create a signature of the zip and send a zip with the
  // // representation files zip and the signature to user
  // else {
  // response.setContentType("application/zip");
  // response.setHeader("Content-Disposition", "attachment; filename="
  // + rep.getLabel() + ".zip");
  // try {
  // // Create needed temporary files
  // File repTempFile = File.createTempFile("root", null);
  // File signatureTempFile = File.createTempFile("sign", null);
  //
  // // Zip representation into the temporary representation file
  // ZipTools.sendZippedRepresentation(request, rep,
  // new FileOutputStream(repTempFile));
  //
  // // Sign the representation zip into the temporary signature file
  // signatureUtility.sign(repTempFile, signatureTempFile);
  //
  // // Create a list with the representation zip and signature
  // List<ZipEntryInfo> files = new ArrayList<ZipEntryInfo>();
  // files
  // .add(new ZipEntryInfo(rep.getLabel() + ".zip",
  // repTempFile));
  // files.add(new ZipEntryInfo(rep.getLabel() + ".p7s",
  // signatureTempFile));
  //
  // // Send the final zip to user
  // response.setContentType("application/zip");
  // response.setHeader("Content-Disposition",
  // "attachment; filename=" + rep.getLabel()
  // + "-signed.zip");
  // ZipTools.zip(files, response.getOutputStream());
  //
  // // Delete temporary files
  // repTempFile.delete();
  // signatureTempFile.delete();
  // } catch (IOException e) {
  // logger.info("User canceled download");
  // }
  // }
  //
  // }
}
