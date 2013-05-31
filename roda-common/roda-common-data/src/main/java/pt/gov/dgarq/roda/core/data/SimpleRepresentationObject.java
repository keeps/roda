package pt.gov.dgarq.roda.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.WrongCModelException;

/**
 * This is a {@link SimpleRepresentationObject}. It contains basic information
 * about a representation.
 * 
 * @author Rui Castro
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

	public static final String[] TYPES = new String[] { DIGITALIZED_WORK,
			STRUCTURED_TEXT, RELATIONAL_DATABASE, VIDEO, AUDIO, UNKNOWN };

	/*
	 * Status
	 */
	public static final String STATUS_ORIGINAL = "original";
	public static final String STATUS_NORMALIZED = "normalized";
	public static final String STATUS_ALTERNATIVE = "alternative";

	public static final String[] STATUSES = new String[] { STATUS_ORIGINAL,
			STATUS_NORMALIZED, STATUS_ALTERNATIVE };
	public static final List<String> STATUSES_LIST = Arrays.asList(STATUSES);

	private String descriptionObjectPID = null;

	// private String[] statuses = null;
	private List<String> statuses = new ArrayList<String>();

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
		this(rObject, rObject.getStatuses(), rObject.getDescriptionObjectPID());
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param rObject
	 * @param statuses
	 * @param descriptionObjectPID
	 */
	public SimpleRepresentationObject(RODAObject rObject, String[] statuses,
			String descriptionObjectPID) {
		this(rObject.getPid(), rObject.getLabel(), rObject.getContentModel(),
				rObject.getLastModifiedDate(), rObject.getCreatedDate(),
				rObject.getState(), statuses, descriptionObjectPID);
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param pid
	 * @param id
	 * @param type
	 * @param subType
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param statuses
	 * @param descriptionObjectPID
	 */
	public SimpleRepresentationObject(String pid, String id, String type,
			String subType, Date lastModifiedDate, Date createdDate,
			String state, String[] statuses, String descriptionObjectPID) {
		super(pid, id, "roda:r", lastModifiedDate, createdDate, state);
		setType(type);
		setSubType(subType);
		setStatuses(statuses);
		setDescriptionObjectPID(descriptionObjectPID);
	}

	/**
	 * Constructs a {@link SimpleRepresentationObject} with the given arguments.
	 * 
	 * @param pid
	 * @param id
	 * @param contentModel
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param statuses
	 * @param descriptionObjectPID
	 */
	public SimpleRepresentationObject(String pid, String id,
			String contentModel, Date lastModifiedDate, Date createdDate,
			String state, String[] statuses, String descriptionObjectPID) {
		super(pid, id, contentModel, lastModifiedDate, createdDate, state);
		setStatuses(statuses);
		setDescriptionObjectPID(descriptionObjectPID);
	}

	/**
	 * @see RODAObject#toString()
	 */
	public String toString() {

		return "SimpleRepresentationObject( " + super.toString()
				+ ", statuses=" + Arrays.toString(getStatuses())
				+ ", descriptionObjectPID=" + getDescriptionObjectPID() + " )";
	}

	/**
	 * @see RODAObject#setContentModel(String)
	 */
	public void setContentModel(String contentModel) {
		super.setContentModel(contentModel);

		String[] names = getContentModel().split(":");
		if (names.length < 3) {
			throw new IllegalArgumentException(contentModel
					+ " is not a valid contentModel for a representation");
		} else {
			if (!names[1].equalsIgnoreCase("r")) {
				throw new WrongCModelException(
						"contentModel should start with 'roda:r' ("
								+ contentModel + ")");
			} else {
				// it's already set, by super.setCModel(contentModel)
				// check that the type is supported

			}
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return getLabel();
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		setLabel(id);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		String type = null;
		if (getContentModel() != null) {
			type = getContentModel().split(":")[2].toLowerCase();
		}
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {

		type = type != null ? type.toLowerCase() : "";

		if (getSubType() != null) {
			setContentModel("roda:r:" + type + ":" + getSubType());
		} else {
			setContentModel("roda:r:" + type);
		}
	}

	/**
	 * @return the type
	 */
	public String getSubType() {

		String subType = null;

		if (getContentModel() != null) {

			String[] split = getContentModel().split(":");

			if (split.length > 3) {
				subType = split[3].length() > 0 ? split[3].toLowerCase() : null;

			}
		}

		return subType;
	}

	/**
	 * @param subType
	 *            the type to set
	 */
	public void setSubType(String subType) {

		String type = getType() != null ? getType() : "";

		if (subType != null) {
			setContentModel("roda:r:" + type + ":" + subType.toLowerCase());
		} else {
			setContentModel("roda:r:" + type);
		}
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
	 * @return the descriptionObjectPID
	 */
	public String getDescriptionObjectPID() {
		return descriptionObjectPID;
	}

	/**
	 * @param descriptionObjectPID
	 *            the descriptionObjectPID to set
	 */
	public void setDescriptionObjectPID(String descriptionObjectPID) {
		this.descriptionObjectPID = descriptionObjectPID;
	}

}
