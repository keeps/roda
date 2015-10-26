/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.dissemination.browse.client;

import java.io.Serializable;

import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.RepresentationPreservationObject;

/**
 * @author Luis Faria
 * 
 */
public class PreservationInfo implements Serializable {

  private static final long serialVersionUID = -4852893408854000715L;

  private String rpoPID;

  private boolean normalized;

  private boolean original;

  private String label;

  private int numberOfFiles;

  private long sizeOfFiles;

  /**
   * Create a new representation info
   */
  public PreservationInfo() {
  }

  /**
   * Create a new representation info
   * 
   * @param roPID
   *          representation persistent id
   * 
   * @param normalized
   *          is the representation normalized
   * @param original
   *          is the representation original
   * @param disseminations
   * @param format
   *          representation format or sub-type
   * @param numberOfFiles
   *          the number of files of the representation
   * @param sizeOfFiles
   *          the sum of the size of all files, in bytes
   */
  public PreservationInfo(String roPID, boolean normalized, boolean original, String format, int numberOfFiles,
    long sizeOfFiles) {
    this.rpoPID = roPID;
    this.normalized = normalized;
    this.original = original;
    this.label = format;
    this.numberOfFiles = numberOfFiles;
    this.sizeOfFiles = sizeOfFiles;
  }

  /**
   * Create a preservation info
   * 
   * @param rpo
   *          the representation preservation object
   * @param normalized
   *          whereas the associated representation is the normalized one
   * @param original
   *          whereas the associated representation is the original one
   */
  public PreservationInfo(RepresentationPreservationObject rpo, boolean normalized, boolean original) {
    // FIXME
    // rpoPID = rpo.getPid();
    // this.normalized = normalized;
    // this.original = original;
    //
    // String cModel = rpo.getRepresentationContentModel();
    // if (cModel != null) {
    // String[] split = cModel.split(":");
    // this.label = split.length >= 4 ? split[3] : rpo.getID();
    // } else {
    // this.label = rpo.getID();
    // }
    //
    // if (rpo.getRootFile() != null) {
    // this.sizeOfFiles = rpo.getRootFile().getSize();
    // } else {
    // this.sizeOfFiles = 0;
    // }
    //
    // this.numberOfFiles = 1;
    // if (rpo.getPartFiles() != null) {
    // this.numberOfFiles += rpo.getPartFiles().length;
    //
    // for (RepresentationFilePreservationObject file : rpo.getPartFiles()) {
    // this.sizeOfFiles += file.getSize();
    // }
    // }
  }

  /**
   * Get representation object id
   * 
   * @return the pid
   */
  public String getRepresentationPreservationObjectPID() {
    return rpoPID;
  }

  /**
   * Set representation object id
   * 
   * @param pid
   */
  public void setPid(String pid) {
    this.rpoPID = pid;
  }

  /**
   * Is the representation normalized
   * 
   * @return true if normalized
   */
  public boolean isNormalized() {
    return normalized;
  }

  /**
   * Set representation as normalized
   * 
   * @param normalized
   */
  public void setNormalized(boolean normalized) {
    this.normalized = normalized;
  }

  /**
   * Is the representation the original
   * 
   * @return true if original
   */
  public boolean isOriginal() {
    return original;
  }

  /**
   * Set representation as the original
   * 
   * @param original
   */
  public void setOriginal(boolean original) {
    this.original = original;
  }

  /**
   * Is this an alternative representation, i. e. not normalized nor original
   * 
   * @return tur if alternative
   */
  public boolean isAlternative() {
    return !normalized && !original;
  }

  /**
   * 
   * @return get preservation info label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Set preservation info label
   * 
   * @param label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * 
   * @return the number of files of the representation
   */
  public int getNumberOfFiles() {
    return numberOfFiles;
  }

  /**
   * Set the number of files of the representation
   * 
   * @param numberOfFiles
   */
  public void setNumberOfFiles(int numberOfFiles) {
    this.numberOfFiles = numberOfFiles;
  }

  /**
   * 
   * @return the sum of the size of all files, in bytes
   */
  public long getSizeOfFiles() {
    return sizeOfFiles;
  }

  /**
   * Set the sum of the size of all files, in bytes
   * 
   * @param sizeOfFiles
   */
  public void setSizeOfFiles(long sizeOfFiles) {
    this.sizeOfFiles = sizeOfFiles;
  }

}
