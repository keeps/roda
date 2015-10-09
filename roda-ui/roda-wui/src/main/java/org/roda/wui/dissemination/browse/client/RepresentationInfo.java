/**
 * 
 */
package org.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.RepresentationObject;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationInfo implements Serializable {

  private static final long serialVersionUID = -4852893408854000715L;

  private String roPID;

  private boolean normalized;

  private boolean original;

  private List<DisseminationInfo> disseminations;

  private String format;

  private int numberOfFiles;

  private long sizeOfFiles;

  /**
   * Create a new representation info
   */
  public RepresentationInfo() {
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
  public RepresentationInfo(String roPID, boolean normalized, boolean original, List<DisseminationInfo> disseminations,
    String format, int numberOfFiles, long sizeOfFiles) {
    this.roPID = roPID;
    this.normalized = normalized;
    this.original = original;
    this.disseminations = disseminations;
    this.format = format;
    this.numberOfFiles = numberOfFiles;
    this.sizeOfFiles = sizeOfFiles;
  }

  /**
   * Create a representation
   * 
   * @param repObj
   *          the representation object
   * @param disseminations
   *          this representation disseminations
   * @param format
   *          representation format or sub-type
   * @param numberOfFiles
   *          the number of files of the representation
   * @param sizeOfFiles
   *          the sum of the size of all files, in bytes
   */
  public RepresentationInfo(RepresentationObject repObj, List<DisseminationInfo> disseminations, String format,
    int numberOfFiles, long sizeOfFiles) {
    Set<String> status = new HashSet<String>(Arrays.asList(repObj.getStatuses()));
    roPID = repObj.getId();
    normalized = status.contains(RepresentationObject.STATUS_NORMALIZED);
    original = status.contains(RepresentationObject.STATUS_ORIGINAL);
    this.disseminations = disseminations;
    this.format = format;
    this.numberOfFiles = numberOfFiles;
    this.sizeOfFiles = sizeOfFiles;
  }

  /**
   * Get representation object id
   * 
   * @return the pid
   */
  public String getRepresentationObjectPID() {
    return roPID;
  }

  /**
   * Set representation object id
   * 
   * @param pid
   */
  public void setPid(String pid) {
    this.roPID = pid;
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
   * Get information of all disseminations
   * 
   * @return a list of dissemination information
   */
  public List<DisseminationInfo> getDisseminations() {
    return disseminations;
  }

  /**
   * Set information of all disseminations
   * 
   * @param disseminations
   */
  public void setDisseminations(List<DisseminationInfo> disseminations) {
    this.disseminations = disseminations;
  }

  /**
   * 
   * @return representation format or sub-type
   */
  public String getFormat() {
    return format;
  }

  /**
   * Set representation format or sub-type
   * 
   * @param format
   */
  public void setFormat(String format) {
    this.format = format;
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
