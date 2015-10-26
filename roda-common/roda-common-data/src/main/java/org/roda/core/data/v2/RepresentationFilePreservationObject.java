/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;

/**
 * This is a Preservation Object for a {@link RepresentationFile}.
 * 
 * @author Rui Castro
 */
public class RepresentationFilePreservationObject extends SimpleRepresentationFilePreservationMetadata implements
  Serializable {
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

  /**
   * The identifier of the preservation object. This field cannot be
   * <code>null</code>.
   */
  private String ID = null;

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

  /*
   * Storage
   */
  private String contentLocationType = null;
  private String contentLocationValue = null;

  /**
   * Constructs a new {@link RepresentationFilePreservationObject}.
   */
  public RepresentationFilePreservationObject() {
    super();
    this.compositionLevel = -1;
  }

  /**
   * Constructs a new {@link RepresentationFilePreservationObject} from a
   * {@link SimpleRepresentationFilePreservationMetadata}.
   */
  public RepresentationFilePreservationObject(SimpleRepresentationFilePreservationMetadata simple) {
    super(simple);
    this.compositionLevel = -1;
  }

  /**
   * Constructs a new {@link RepresentationFilePreservationObject} cloning an
   * existing {@link RepresentationFilePreservationObject}.
   * 
   * @param filePO
   */
  public RepresentationFilePreservationObject(RepresentationFilePreservationObject filePO) {
    this(filePO.getID(), filePO.getPreservationLevel(), filePO.getCompositionLevel(), filePO.getFixities(), filePO
      .getSize(), filePO.getFormatDesignationName(), filePO.getFormatDesignationVersion(), filePO
      .getFormatRegistryName(), filePO.getFormatRegistryKey(), filePO.getFormatRegistryRole(), filePO
      .getCreatingApplicationName(), filePO.getCreatingApplicationVersion(), filePO.getDateCreatedByApplication(),
      filePO.getOriginalName(), filePO.getObjectCharacteristicsExtension(), filePO.getContentLocationType(), filePO
        .getContentLocationValue());
  }

  /**
   * Constructs a new {@link RepresentationFilePreservationObject} with the
   * given parameters.
   * 
   * @param id
   *          the identifier of the preservation object
   * @param preservationLevel
   * @param compositionLevel
   * @param fixities
   * @param size
   * @param formatDesignationName
   * @param formatDesignationVersion
   * @param formatRegistryName
   * @param formatRegistryKey
   * @param formatRegistryRole
   * @param creatingApplicationName
   * @param creatingApplicationVersion
   * @param dateCreatedByApplication
   * @param originalName
   * @param objectCharacteristicsExtension
   * @param contentLocationType
   * @param contentLocationValue
   */
  public RepresentationFilePreservationObject(String id, String preservationLevel, int compositionLevel,
    Fixity[] fixities, long size, String formatDesignationName, String formatDesignationVersion,
    String formatRegistryName, String formatRegistryKey, String formatRegistryRole, String creatingApplicationName,
    String creatingApplicationVersion, String dateCreatedByApplication, String originalName,
    String objectCharacteristicsExtension, String contentLocationType, String contentLocationValue) {

    setID(id);
    setPreservationLevel(preservationLevel);

    setCompositionLevel(compositionLevel);
    setFixities(fixities);
    setSize(size);

    setFormatDesignationName(formatDesignationName);
    setFormatDesignationVersion(formatDesignationVersion);

    setFormatRegistryName(formatRegistryName);
    setFormatRegistryKey(formatRegistryKey);
    setFormatRegistryRole(formatRegistryRole);

    setCreatingApplicationName(creatingApplicationName);
    setCreatingApplicationVersion(creatingApplicationVersion);
    setDateCreatedByApplication(dateCreatedByApplication);

    setOriginalName(originalName);

    setObjectCharacteristicsExtension(objectCharacteristicsExtension);

    setContentLocationType(contentLocationType);
    setContentLocationValue(contentLocationValue);
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

      return getID() == other.getID() || getID().equals(other.getID());

    } else {
      return false;
    }
  }

  /**
   * @return a {@link String} with this object's info.
   * @see PreservationObject#toString()
   */
  @Override
  public String toString() {
    return "RepresentationFilePreservationObject(ID=" + getID() + ", size=" + getSize() + ", formatDesignationName="
      + getFormatDesignationName() + ", formatDesignationVersion=" + getFormatDesignationVersion()
      + ", formatRegistryName=" + getFormatRegistryName() + ", formatRegistryKey=" + getFormatRegistryKey()
      + ", formatRegistryRole=" + getFormatRegistryRole() + ", originalName=" + getOriginalName() + ")";
  }

  /**
   * The identifier of the preservation object.
   * 
   * @return the identifier of the preservation object as a {@link String}.
   */
  public String getID() {
    return ID;
  }

  /**
   * Sets the identifier of the preservation object.
   * 
   * @param id
   *          the identifier to set.
   * @throws NullPointerException
   *           if ID is <code>null</code>.
   */
  public void setID(String id) {
    if (id == null) {
      throw new NullPointerException("ID cannot be null");
    } else {
      ID = id;
    }
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
}
