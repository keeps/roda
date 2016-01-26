/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This is a Preservation Object for a {@link RepresentationFile}.
 * 
 * @author Rui Castro
 */
public class RepresentationFilePreservationObject extends PreservationObject implements Serializable {
  private static final long serialVersionUID = 3984141759920127217L;
  /**
   * Full preservation level, from file integrity, file format management, to
   * intellectual value preservation
   */
  public static final String PRESERVATION_LEVEL_FULL = "full";

  /**
   * Only file integrity is preserved
   */
  public static final String PRESERVATION_LEVEL_BITLEVEL = "bitlevel";

  private String preservationLevel = null;

  /*
   * <objectCharacteristics>
   */
  private int compositionLevel = 0;

  private Fixity[] fixities = null;

  private String formatDesignationName = null;
  private String formatDesignationVersion = null;

  private String formatRegistryName = null;
  private String formatRegistryKey = null;
  private String formatRegistryRole = null;

  private String creatingApplicationName = null;
  private String creatingApplicationVersion = null;
  private String dateCreatedByApplication = null;

  private String originalName = null;

  private String objectCharacteristicsExtension = null;

  private String contentLocationType = null;
  private String contentLocationValue = null;

  private String pronomId;
  private String mimetype;
  private long size;
  private String hash;

  private String representationObjectId;

  /**
   * Constructs a new {@link RepresentationFilePreservationObject}.
   */
  public RepresentationFilePreservationObject() {
    super();
    this.compositionLevel = -1;
  }

  /**
   * @param obj
   * 
   * @return <code>true</code> if the objects are equal and <code>false</code>
   *         otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof RepresentationFilePreservationObject) {
      RepresentationFilePreservationObject other = (RepresentationFilePreservationObject) obj;

      return getId() == other.getId() || getId().equals(other.getId());

    } else {
      return false;
    }
  }

 

  @Override
  public String toString() {
    return "RepresentationFilePreservationObject [preservationLevel=" + preservationLevel + ", compositionLevel="
      + compositionLevel + ", fixities=" + Arrays.toString(fixities) + ", formatDesignationName="
      + formatDesignationName + ", formatDesignationVersion=" + formatDesignationVersion + ", formatRegistryName="
      + formatRegistryName + ", formatRegistryKey=" + formatRegistryKey + ", formatRegistryRole=" + formatRegistryRole
      + ", creatingApplicationName=" + creatingApplicationName + ", creatingApplicationVersion="
      + creatingApplicationVersion + ", dateCreatedByApplication=" + dateCreatedByApplication + ", originalName="
      + originalName + ", objectCharacteristicsExtension=" + objectCharacteristicsExtension + ", contentLocationType="
      + contentLocationType + ", contentLocationValue=" + contentLocationValue + ", pronomId=" + pronomId
      + ", mimetype=" + mimetype + ", size=" + size + ", hash=" + hash + ", representationObjectId="
      + representationObjectId + "]";
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
   * @return the compositionLevel
   */
  public int getCompositionLevel() {
    return compositionLevel;
  }

  /**
   * @param compositionLevel
   *          the compositionLevel to set
   */
  public void setCompositionLevel(int compositionLevel) {
    this.compositionLevel = compositionLevel;
  }

  /**
   * @return the fixities
   */
  public Fixity[] getFixities() {
    return fixities;
  }

  /**
   * @param fixities
   *          the fixities to set
   */
  public void setFixities(Fixity[] fixities) {
    this.fixities = fixities;
  }

  /**
   * @return the formatDesignationName
   */
  public String getFormatDesignationName() {
    return formatDesignationName;
  }

  /**
   * @param formatDesignationName
   *          the formatDesignationName to set
   */
  public void setFormatDesignationName(String formatDesignationName) {
    this.formatDesignationName = formatDesignationName;
  }

  /**
   * @return the formatDesignationVersion
   */
  public String getFormatDesignationVersion() {
    return formatDesignationVersion;
  }

  /**
   * @param formatDesignationVersion
   *          the formatDesignationVersion to set
   */
  public void setFormatDesignationVersion(String formatDesignationVersion) {
    this.formatDesignationVersion = formatDesignationVersion;
  }

  /**
   * @return the formatRegistryName
   */
  public String getFormatRegistryName() {
    return formatRegistryName;
  }

  /**
   * @param formatRegistryName
   *          the formatRegistryName to set
   */
  public void setFormatRegistryName(String formatRegistryName) {
    this.formatRegistryName = formatRegistryName;
  }

  /**
   * @return the formatRegistryKey
   */
  public String getFormatRegistryKey() {
    return formatRegistryKey;
  }

  /**
   * @param formatRegistryKey
   *          the formatRegistryKey to set
   */
  public void setFormatRegistryKey(String formatRegistryKey) {
    this.formatRegistryKey = formatRegistryKey;
  }

  /**
   * @return the formatRegistryRole
   */
  public String getFormatRegistryRole() {
    return formatRegistryRole;
  }

  /**
   * @param formatRegistryRole
   *          the formatRegistryRole to set
   */
  public void setFormatRegistryRole(String formatRegistryRole) {
    this.formatRegistryRole = formatRegistryRole;
  }

  /**
   * @return the creatingApplicationName
   */
  public String getCreatingApplicationName() {
    return creatingApplicationName;
  }

  /**
   * @param creatingApplicationName
   *          the creatingApplicationName to set
   */
  public void setCreatingApplicationName(String creatingApplicationName) {
    this.creatingApplicationName = creatingApplicationName;
  }

  /**
   * @return the creatingApplicationVersion
   */
  public String getCreatingApplicationVersion() {
    return creatingApplicationVersion;
  }

  /**
   * @param creatingApplicationVersion
   *          the creatingApplicationVersion to set
   */
  public void setCreatingApplicationVersion(String creatingApplicationVersion) {
    this.creatingApplicationVersion = creatingApplicationVersion;
  }

  /**
   * @return the dateCreatedByApplication
   */
  public String getDateCreatedByApplication() {
    return dateCreatedByApplication;
  }

  /**
   * @param dateCreatedByApplication
   *          the dateCreatedByApplication to set
   */
  public void setDateCreatedByApplication(String dateCreatedByApplication) {
    this.dateCreatedByApplication = dateCreatedByApplication;
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
   * @return the objectCharacteristicsExtension
   */
  public String getObjectCharacteristicsExtension() {
    return objectCharacteristicsExtension;
  }

  /**
   * @param objectCharacteristicsExtension
   *          the objectCharacteristicsExtension to set
   */
  public void setObjectCharacteristicsExtension(String objectCharacteristicsExtension) {
    this.objectCharacteristicsExtension = objectCharacteristicsExtension;
  }

  /**
   * @return the contentLocationType
   */
  public String getContentLocationType() {
    return contentLocationType;
  }

  /**
   * @param contentLocationType
   *          the contentLocationType to set
   */
  public void setContentLocationType(String contentLocationType) {
    this.contentLocationType = contentLocationType;
  }

  /**
   * @return the contentLocationValue
   */
  public String getContentLocationValue() {
    return contentLocationValue;
  }

  /**
   * @param contentLocationValue
   *          the contentLocationValue to set
   */
  public void setContentLocationValue(String contentLocationValue) {
    this.contentLocationValue = contentLocationValue;
  }

  public String getPronomId() {
    return pronomId;
  }

  public void setPronomId(String pronomId) {
    this.pronomId = pronomId;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getRepresentationObjectId() {
    return representationObjectId;
  }

  public void setRepresentationObjectId(String representationObjectId) {
    this.representationObjectId = representationObjectId;
  }

}
