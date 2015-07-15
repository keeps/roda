package org.roda.legacy.aip.metadata.descriptive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.data.v2.RODAObject;

/**
 * This is a {@link SimpleRepresentationObject}. It contains basic information
 * about a representation.
 * 
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class SimpleRepresentationObject extends RODAObject {

	private static final long serialVersionUID = 5693442011598801608L;

	/*
	 * Types
	 */
	public static final String UNKNOWN = "unknown";
	public static final String DIGITALIZED_WORK = "digitalized_work";
	public static final String STRUCTURED_TEXT = "structured_text";
	public static final String RELATIONAL_DATABASE = "relational_database";
	public static final String VIDEO = "video";
	public static final String AUDIO = "audio";
	public static final String EMAIL = "email";
	public static final String PRESENTATION = "presentation";
	public static final String SPREADSHEET = "spreadsheet";
	public static final String VECTOR_GRAPHIC = "vector_graphic";
	public static final String APPLICATION_DATA = "application_data";
	public static final String[] TYPES = new String[] { EMAIL, STRUCTURED_TEXT,
			PRESENTATION, SPREADSHEET, VECTOR_GRAPHIC, DIGITALIZED_WORK, AUDIO,
			VIDEO, RELATIONAL_DATABASE, APPLICATION_DATA, UNKNOWN };

	/*
	 * Status
	 */
	public static final String STATUS_ORIGINAL = "original";
	public static final String STATUS_NORMALIZED = "normalized";
	public static final String STATUS_ALTERNATIVE = "alternative";
	public static final String[] STATUSES = new String[] { STATUS_ORIGINAL,
			STATUS_NORMALIZED, STATUS_ALTERNATIVE };
	public static final List<String> STATUSES_LIST = Arrays.asList(STATUSES);
	private String descriptionObjectID = null;
	// private String[] statuses = null;
	private List<String> statuses = new ArrayList<String>();
	private String type;
	private String subType;

	/**
	 * Constructs an empty {@link SimpleRepresentationObject}.
	 */
	public SimpleRepresentationObject() {
		setType(UNKNOWN);
	}

	/**
	 * Constructs a new {@link SimpleRepresentationObject} cloning an existing
	 * {@link SimpleRepresentationObject}.
	 * 
	 * @param rObject
	 *            a Representation Object.
	 */
	public SimpleRepresentationObject(SimpleRepresentationObject rObject) {
		this(rObject, rObject.getStatuses(), rObject.getDescriptionObjectID());
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param rObject
	 * @param statuses
	 * @param descriptionObjectID
	 */
	public SimpleRepresentationObject(RODAObject rObject, String[] statuses,
			String descriptionObjectID) {
		this(rObject.getId(), rObject.getLabel(),
				rObject.getLastModifiedDate(), rObject.getCreatedDate(),
				rObject.getState(), statuses, descriptionObjectID);
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param id
	 * @param type
	 * @param subType
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param statuses
	 * @param descriptionObjectID
	 */
	public SimpleRepresentationObject(String id, String label, String type,
			String subType, Date lastModifiedDate, Date createdDate,
			String state, String[] statuses, String descriptionObjectID) {
		super(id, label, lastModifiedDate, createdDate, state);
		setType(type);
		setSubType(subType);
		setStatuses(statuses);
		setDescriptionObjectID(descriptionObjectID);
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param label
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param statuses
	 * @param descriptionObjectID
	 */
	public SimpleRepresentationObject(String id, String label,
			Date lastModifiedDate, Date createdDate, String state,
			String[] statuses, String descriptionObjectID) {
		super(id, label, lastModifiedDate, createdDate, state);
		setStatuses(statuses);
		setDescriptionObjectID(descriptionObjectID);
	}

	/**
	 * @see RODAObject#toString()
	 */
	@Override
	public String toString() {

		return "SimpleRepresentationObject( " + super.toString()
				+ ", statuses=" + Arrays.toString(getStatuses())
				+ ", descriptionObjectID=" + getDescriptionObjectID() + " )";
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return getLabel();
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(String id) {
		setLabel(id);
	}

	/**
	 * @return the statuses
	 */
	public String[] getStatuses() {
		return statuses.toArray(new String[statuses.size()]);
	}

	/**
	 * @param statuses
	 *            the statuses to set
	 * 
	 * @exception NullPointerException
	 * @exception IllegalArgumentException
	 */
	public void setStatuses(String[] statuses) throws NullPointerException,
			IllegalArgumentException {

		if (statuses == null) {
			throw new NullPointerException("statuses cannot be null");
		} else if (statuses.length == 0) {
			throw new IllegalArgumentException("statuses cannot be empty");
		} else {

			this.statuses.clear();

			for (int i = 0; i < statuses.length; i++) {
				addStatus(statuses[i]);
			}

		}
	}

	/**
	 * Add a new status to the representation.
	 * 
	 * @param status
	 *            the status to add.
	 */
	public void addStatus(String status) {
		if (status == null) {
			throw new NullPointerException("statuses cannot be null");
		} else {

			if (STATUSES_LIST.contains(status)) {
				this.statuses.add(status);
			} else {
				throw new IllegalArgumentException("'" + status
						+ "' is not a valid status value.");
			}

		}
	}

	/**
	 * Remove a status from the representation.
	 * 
	 * @param status
	 *            the status to remove.
	 */
	public void removeStatus(String status) {
		this.statuses.remove(status);
	}

	/**
	 * @return the descriptionObjectID
	 */
	public String getDescriptionObjectID() {
		return descriptionObjectID;
	}

	/**
	 * @param descriptionObjectID
	 *            the descriptionObjectID to set
	 */
	public void setDescriptionObjectID(String descriptionObjectID) {
		this.descriptionObjectID = descriptionObjectID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

}
