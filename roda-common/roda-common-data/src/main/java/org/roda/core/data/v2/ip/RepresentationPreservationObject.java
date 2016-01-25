/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.Date;

import org.roda.core.data.v2.ip.metadata.PreservationObject;

/**
 * This is a representation preservation object. It contains preservation
 * metadata about a representation.
 * 
 * @author Rui Castro
 */
public class RepresentationPreservationObject extends PreservationObject {
  public static final String PRESERVATION_LEVEL_FULL = "full";
  public static final String PRESERVATION_LEVEL_BITLEVEL = "bitlevel";

  private String preservationLevel = null;

  private String representationObjectID;

  /**
   * The representation "entry point" file.
   */
  private RepresentationFilePreservationObject rootFile = null;

  /**
   * The representation part files. This list can be <code>null</code> or empty.
   */
  private RepresentationFilePreservationObject[] partFiles = null;

  /**
   * The ID of the representation from which this representation derived. This
   * field can be <code>null</code>.
   */
  private String derivedFromRepresentationObjectID = null;

  /**
   * The ID of the event that created this derivative representation. This field
   * can be <code>null</code>, but if it's not <code>null</code>, than
   * {@link RepresentationPreservationObject#derivedFromRepresentationObjectID}
   * cannot be <code>null</code> either.
   */
  private String derivationEventID = null;

  /**
   * A list with the IDs of the preservation events that occurred over this
   * representation. This list can be <code>null</code> or empty.
   */
  private String[] preservationEventIDs = null;

  /**
   * Constructs an empty {@link RepresentationPreservationObject}.
   */
  public RepresentationPreservationObject() {
    super();
  }

  /**
   * @param id
   * @param label
   * @param model
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param ID
   */
  public RepresentationPreservationObject(String id, String label, String model, Date lastModifiedDate,
    Date createdDate, String state, String ID) {
    super();
    setId(id);
    setLabel(label);
    setLastModifiedDate(lastModifiedDate);
    setCreatedDate(createdDate);
    setState(state);
  }

  /**
   * @see PreservationObject#toString()
   */
  @Override
  public String toString() {

    int partFilesCount = (getPartFiles() != null) ? getPartFiles().length : 0;

    return "RepresentationPreservationObject( " + super.toString() + ", preservationLevel=" + getPreservationLevel()
      + ", rootFile=" + getRootFile() + ", partFiles=" + partFilesCount + ", derivedFromRepresentationObjectID="
      + getDerivedFromRepresentationObjectID() + ", derivationEventID=" + getDerivationEventID()
      + ", preservationEventIDs=" + Arrays.toString(getPreservationEventIDs()) + " )";

  }

  /**
   * @return the preservationLevel
   */
  public String getPreservationLevel() {
    return preservationLevel;
  }

  /**
   * @param preservationLevel
   *          the preservationLevel to set
   */
  public void setPreservationLevel(String preservationLevel) {
    this.preservationLevel = preservationLevel;
  }

  /**
   * @return the rootFile
   */
  public RepresentationFilePreservationObject getRootFile() {
    return rootFile;
  }

  /**
   * @param rootFile
   *          the rootFile to set
   * 
   * @throws NullPointerException
   *           if <param>rootFile</param> is <code>null</code>.
   */
  public void setRootFile(RepresentationFilePreservationObject rootFile) throws NullPointerException {
    this.rootFile = rootFile;
    // if (rootFile == null) {
    // throw new NullPointerException("rootFile cannot be null"); //$NON-NLS-1$
    // } else {
    // this.rootFile = rootFile;
    // }
  }

  /**
   * @return the partFiles
   */
  public RepresentationFilePreservationObject[] getPartFiles() {
    return partFiles;
  }

  /**
   * @param partFiles
   *          the partFiles to set
   */
  public void setPartFiles(RepresentationFilePreservationObject[] partFiles) {
    this.partFiles = partFiles;
  }

  /**
   * @return the derivedFromRepresentationObjectID
   */
  public String getDerivedFromRepresentationObjectID() {
    return derivedFromRepresentationObjectID;
  }

  /**
   * @param derivedFromRepresentationObjectID
   *          the derivedFromRepresentationObjectID to set
   */
  public void setDerivedFromRepresentationObjectID(String derivedFromRepresentationObjectID) {
    this.derivedFromRepresentationObjectID = derivedFromRepresentationObjectID;
  }

  /**
   * @return the derivationEventID
   */
  public String getDerivationEventID() {
    return derivationEventID;
  }

  /**
   * @param derivationEventID
   *          the derivationEventID to set
   */
  public void setDerivationEventID(String derivationEventID) {
    this.derivationEventID = derivationEventID;
  }

  /**
   * @return the preservationEventIDs
   */
  public String[] getPreservationEventIDs() {
    return preservationEventIDs;
  }

  /**
   * @param preservationEventIDs
   *          the preservationEventIDs to set
   */
  public void setPreservationEventIDs(String[] preservationEventIDs) {
    this.preservationEventIDs = preservationEventIDs;
  }

  public String getRepresentationObjectID() {
    return representationObjectID;
  }

  public void setRepresentationObjectID(String representationObjectID) {
    this.representationObjectID = representationObjectID;
  }

}
