/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata.v2.premis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.roda.core.metadata.MetadataException;

import lc.xmlns.premisV2.ObjectDocument;

/**
 * Base class for all PREMIS Object helpers: Representation, File and Bitstream
 * (not used).
 * 
 * @author Rui Castro
 */
public abstract class PremisObjectHelper {

  private static final Logger logger = Logger.getLogger(PremisObjectHelper.class);

  // TODO these constants should go into a configuration file
  protected static final String premisRelationshipTypeStructural = "structural";
  protected static final String premisRelationshipSubTypeHasRoot = "has root";
  protected static final String premisRelationshipSubTypeHasPart = "has part";

  protected static final String premisRelationshipTypeDerivation = "derivation";
  protected static final String premisRelationshipSubTypeDerivedFrom = "derived from";

  private final ObjectDocument objectDocument;

  /**
   * Create a new PREMIS Object helper
   * 
   * @param objectDocument
   *          the PREMIS {@link ObjectDocument}.
   */
  public PremisObjectHelper(ObjectDocument objectDocument) {
    this.objectDocument = objectDocument;
  }

  /**
   * Returns the current {@link ObjectDocument}.
   * 
   * @return a {@link ObjectDocument}.
   */
  public ObjectDocument getObjectDocument() {
    return objectDocument;
  }

  /**
   * Saves the current PREMIS document to a byte array.
   * 
   * @return a <code>byte[]</code> with the contents of the PREMIS XML file.
   * 
   * @throws PremisMetadataException
   *           if the PREMIS document is not valid or if something goes wrong
   *           with the serialisation.
   */
  public byte[] saveToByteArray() throws PremisMetadataException {

    try {

      return MetadataHelperUtility.saveToByteArray(getObjectDocument());

    } catch (MetadataException e) {
      logger.debug(e.getMessage(), e);
      throw new PremisMetadataException(e.getMessage(), e);
    }
  }

  /**
   * Saves the current PREMIS document to a {@link File}.
   * 
   * @param premisFile
   *          the {@link File}.
   * 
   * @throws PremisMetadataException
   *           if the EAD-C document is not valid or if something goes wrong
   *           with the serialisation.
   * 
   * @throws IOException
   *           if {@link FileOutputStream} associated with the {@link File}
   *           couldn't be closed.
   * @throws IOException
   */
  public void saveToFile(File premisFile) throws PremisMetadataException, IOException {
    try {

      MetadataHelperUtility.saveToFile(getObjectDocument(), premisFile);

    } catch (MetadataException e) {
      logger.debug(e.getMessage(), e);
      throw new PremisMetadataException(e.getMessage(), e);
    }
  }

}
