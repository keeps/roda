package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;

/**
 * This is a file inside a {@link RepresentationObject}.
 * 
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class RepresentationFile implements Serializable {

  private static final long serialVersionUID = 542428357688741774L;
  private String id = null;
  private String originalName = null;
  private long size = 0;
  private String accessURL = null;
  private FileFormat fileFormat = null;

  /**
   * Constructs an empty {@link RepresentationFile}.
   */
  public RepresentationFile() {
    fileFormat = new FileFormat();
  }

  public RepresentationFile(String id, String originalName, long size, String accessURL, FileFormat fileFormat) {
    setFileFormat(fileFormat);
    setId(id);
    setOriginalName(originalName);
    setSize(size);
    setAccessURL(accessURL);
  }

  /**
   * Construct a {@link RepresentationFile} cloning an existing
   * {@link RepresentationFile}.
   * 
   * @param representationFile
   *          the {@link RepresentationFile} to clone.
   */
  public RepresentationFile(RepresentationFile representationFile) {
    this(representationFile.getId(), representationFile.getOriginalName(), representationFile.getSize(),
      representationFile.getAccessURL(), representationFile.getFileFormat());
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("RepresentationFile(id=");
    strBuilder.append(getId());
    strBuilder.append(", originalName=");
    strBuilder.append(getOriginalName());
    strBuilder.append(", size=");
    strBuilder.append(getSize());
    strBuilder.append(", accessURL=");
    strBuilder.append(getAccessURL());
    strBuilder.append(", fileformat:mimetype=");
    strBuilder.append(fileFormat.getMimetype());
    strBuilder.append(", fileformat:name=");
    strBuilder.append(fileFormat.getName());
    strBuilder.append(", fileformat:version=");
    strBuilder.append(fileFormat.getVersion());
    strBuilder.append(", fileformat:fomratRegistryName=");
    strBuilder.append(fileFormat.getFormatRegistryName());
    strBuilder.append(", fileformat:puid=");
    strBuilder.append(fileFormat.getPuid());
    strBuilder.append(")");
    return strBuilder.toString();
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RepresentationFile) {
      RepresentationFile other = (RepresentationFile) obj;
      return getId() == other.getId() || getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    if (id == null) {
      throw new NullPointerException("id cannot be null");
    }
    this.id = id;
  }

  /**
   * @return the originalName
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * @param originalName
   *          the originalName to set
   */
  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  /**
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @param size
   *          the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * @return the accessURL
   */
  public String getAccessURL() {
    return accessURL;
  }

  /**
   * @param accessURL
   *          the accessURL to set
   */
  public void setAccessURL(String accessURL) {
    this.accessURL = accessURL;
  }

  public FileFormat getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(FileFormat fileFormat) {
    this.fileFormat = fileFormat;
  }

  public String getMimetype() {
    return fileFormat.getMimetype();
  }

  public void setMimetype(String mimetype) {
    fileFormat.setMimetype(mimetype);
  }

  public FileFormat exportFileFormat() {
    return fileFormat.exportFileFormat();
  }

  public void importFileFormat(FileFormat fileFormat) {
    this.fileFormat.importFileFormat(fileFormat);
  }

  public String getPuid() {
    return fileFormat.getPuid();
  }

  public String[] getExtensions() {
    return fileFormat.getExtensions();
  }

  public String getFormatRegistryName() {
    return fileFormat.getFormatRegistryName();
  }

  public String getName() {
    return fileFormat.getName();
  }

  public String getVersion() {
    return fileFormat.getVersion();
  }
}
