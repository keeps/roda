package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;

/**
 * @author sleroux
 *
 */
public class PreservationMetadataBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private String id;
  private String html;
  private long sizeInBytes;
  private int numberOfFiles;

  public PreservationMetadataBundle() {
    super();
  }

  public PreservationMetadataBundle(String id, String html, long sizeInBytes, int numberOfFiles) {
    super();
    this.id = id;
    this.html = html;
    this.sizeInBytes = sizeInBytes;
    this.numberOfFiles = numberOfFiles;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
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
