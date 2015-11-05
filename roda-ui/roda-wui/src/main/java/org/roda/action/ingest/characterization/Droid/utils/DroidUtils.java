/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.characterization.Droid.utils;

import java.nio.file.Path;

import org.apache.log4j.Logger;

public class DroidUtils {
  private final Logger logger = Logger.getLogger(getClass());
  private Path signature;
  private int maxBytesToScan = -1;
  // private BinarySignatureIdentifier binarySignatureIdentifier;
  //
  // private static DroidUtils instance;
  //
  // public static DroidUtils getInstance(Path signaturePath) throws
  // DroidException {
  // if (instance == null) {
  // instance = new DroidUtils(signaturePath);
  // }
  // return instance;
  // }
  //
  // private DroidUtils(Path signaturePath) throws DroidException {
  // this.signature = signaturePath;
  // binarySignatureIdentifier = new BinarySignatureIdentifier();
  // logger.debug("SIGNATURE: " + signature.toAbsolutePath().toString());
  // binarySignatureIdentifier.setSignatureFile(signature.toAbsolutePath().toString());
  // try {
  // binarySignatureIdentifier.init();
  // } catch (SignatureParseException e) {
  // throw new DroidException("Can't parse signature file");
  // }
  // binarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);
  // }
  //
  // public FileFormat execute(Path pathToIdentify) throws DroidException,
  // IOException {
  // FileFormat fileFormat = null;
  //
  // String fileName = pathToIdentify.toFile().getCanonicalPath();
  // URI uri = pathToIdentify.toUri();
  // RequestMetaData metaData = new RequestMetaData(Files.size(pathToIdentify),
  // Files.getLastModifiedTime(pathToIdentify).toMillis(), fileName);
  // RequestIdentifier identifier = new RequestIdentifier(uri);
  // identifier.setParentId(1L);
  //
  // InputStream in = null;
  // IdentificationRequest request = new
  // FileSystemIdentificationRequest(metaData, identifier);
  // try {
  // in = Files.newInputStream(pathToIdentify);
  // request.open(in);
  // IdentificationResultCollection results =
  // binarySignatureIdentifier.matchBinarySignatures(request);
  //
  // // Remove format duplicates, format with lower priority is removed
  // binarySignatureIdentifier.removeLowerPriorityHits(results);
  //
  // if (results.getResults().size() < 1)
  // throw new DroidException("Unknown format.");
  // else if (results.getResults().size() > 1) {
  // StringBuilder strBuilder = new StringBuilder();
  // for (IdentificationResult result : results.getResults())
  // strBuilder.append(result.getPuid());
  // strBuilder.append(", ");
  // throw new DroidException(String
  // .format("More then one format detected, unknown format. Detected PRONOM
  // PUIDs: %s", strBuilder.toString()));
  // } else {
  // // One format detected
  // // TODO: check that all fileFormat data are fill properly
  // IdentificationResult result = results.getResults().get(0);
  // fileFormat = new FileFormat();
  // fileFormat.setName(result.getMimeType());
  // fileFormat.setMimetype(result.getMimeType());
  // fileFormat.setPuid(result.getPuid());
  // fileFormat.setVersion(result.getVersion());
  // }
  // } finally {
  // if (in != null) {
  // in.close();
  // }
  // }
  // return fileFormat;
  // }
}