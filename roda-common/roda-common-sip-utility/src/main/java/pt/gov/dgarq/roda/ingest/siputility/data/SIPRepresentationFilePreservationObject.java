package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;

import pt.gov.dgarq.roda.core.data.preservation.Fixity;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;

/**
 * @author Rui Casrtro
 * 
 */
public class SIPRepresentationFilePreservationObject extends
		RepresentationFilePreservationObject {
	private static final long serialVersionUID = -1927846431296286356L;

	private File premisFile = null;

	/**
	 * Constructs a new {@link SIPRepresentationFilePreservationObject}.
	 */
	public SIPRepresentationFilePreservationObject() {
	}

	/**
	 * Constructs a new {@link SIPRepresentationFilePreservationObject}.
	 * 
	 * @param filePO
	 */
	public SIPRepresentationFilePreservationObject(
			RepresentationFilePreservationObject filePO) {
		super(filePO);
	}

	/**
	 * Constructs a new {@link SIPRepresentationFilePreservationObject}.
	 * 
	 * @param id
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
	public SIPRepresentationFilePreservationObject(String id,
			String preservationLevel, int compositionLevel, Fixity[] fixities,
			long size, String formatDesignationName,
			String formatDesignationVersion, String formatRegistryName,
			String formatRegistryKey, String formatRegistryRole,
			String creatingApplicationName, String creatingApplicationVersion,
			String dateCreatedByApplication, String originalName,
			String objectCharacteristicsExtension, String contentLocationType,
			String contentLocationValue) {
		super(id, preservationLevel, compositionLevel, fixities, size,
				formatDesignationName, formatDesignationVersion,
				formatRegistryName, formatRegistryKey, formatRegistryRole,
				creatingApplicationName, creatingApplicationVersion,
				dateCreatedByApplication, originalName,
				objectCharacteristicsExtension, contentLocationType,
				contentLocationValue);
	}

	/**
	 * @see RepresentationFilePreservationObject#toString()
	 */
	@Override
	public String toString() {
		return "SIPRepresentationFilePreservationObject( premisFile=" //$NON-NLS-1$
				+ getPremisFile() + ", " + super.toString() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see RepresentationFilePreservationObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null
				&& obj instanceof SIPRepresentationFilePreservationObject) {
			SIPRepresentationFilePreservationObject other = (SIPRepresentationFilePreservationObject) obj;

			if (getPremisFile() == null) {
				return super.equals(other);
			} else {
				return getPremisFile().equals(other.getPremisFile())
						&& super.equals(other);
			}

		} else {
			return false;
		}
	}

	/**
	 * @return the premisFile
	 */
	public File getPremisFile() {
		return premisFile;
	}

	/**
	 * @param premisFile
	 *            the premisFile to set
	 */
	public void setPremisFile(File premisFile) {
		this.premisFile = premisFile;
	}

}
