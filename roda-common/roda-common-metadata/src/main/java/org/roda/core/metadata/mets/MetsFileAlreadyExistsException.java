/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata.mets;

import org.roda.core.metadata.MetadataException;

import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;

/**
 * Thrown to indicate that a {@link FileType} already exists inside a
 * {@link MetsDocument}.
 * 
 * @author Luís Faria
 * @author Rui Castro
 */
public class MetsFileAlreadyExistsException extends MetadataException {
  private static final long serialVersionUID = -6804558406230272070L;

  /**
   * Construct a new {@link MetsFileAlreadyExistsException}.
   */
  public MetsFileAlreadyExistsException() {
    super();
  }

  /**
   * Construct a new {@link MetsFileAlreadyExistsException} with the given
   * message.
   * 
   * @param message
   *          the error message.
   */
  public MetsFileAlreadyExistsException(String message) {
    super(message);
  }

}
