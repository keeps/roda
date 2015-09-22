package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;

/**
 * @author sleroux
 *
 */
public class PreservationMetadataBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private long sizeInBytes;
  private int numberOfFiles;

  public PreservationMetadataBundle() {
    super();
  }

  public PreservationMetadataBundle(long sizeInBytes, int numberOfFiles) {
    super();
    this.sizeInBytes = sizeInBytes;
    this.numberOfFiles = numberOfFiles;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  public int getNumberOfFiles() {
    return numberOfFiles;
  }

  public void setNumberOfFiles(int numberOfFiles) {
    this.numberOfFiles = numberOfFiles;
  }

}
