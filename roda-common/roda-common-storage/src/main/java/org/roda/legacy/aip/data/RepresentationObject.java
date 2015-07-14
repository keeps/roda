package org.roda.legacy.aip.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.legacy.aip.metadata.RODAObject;
import org.roda.legacy.aip.metadata.descriptive.SimpleRepresentationObject;

/**
 * This is a {@link RepresentationObject}. It contains the information about a
 * representation.
 * 
 * @author Rui Castro
 */
public class RepresentationObject extends SimpleRepresentationObject {
	private static final long serialVersionUID = -6678725617872116912L;

	private RepresentationFile rootFile = null;
	private List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();

	/**
	 * Constructs an empty {@link RepresentationObject}.
	 */
	public RepresentationObject() {
	}

	/**
	 * Constructs a new {@link RepresentationObject} cloning an existing
	 * {@link SimpleRepresentationObject}.
	 * 
	 * @param simpleRO
	 *            a {@link SimpleRepresentationObject}.
	 */
	public RepresentationObject(SimpleRepresentationObject simpleRO) {
		super(simpleRO);
	}

	/**
	 * Constructs a new {@link RepresentationObject} with the given parameters.
	 * 
	 * @param simpleRO
	 * @param rootFile
	 * @param partFiles
	 */
	public RepresentationObject(SimpleRepresentationObject simpleRO,
			RepresentationFile rootFile, RepresentationFile[] partFiles) {
		super(simpleRO);
		setRootFile(rootFile);
		setPartFiles(partFiles);
	}

	/**
	 * Constructs a new {@link RepresentationObject} cloning an existing
	 * {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            a Representation Object.
	 */
	public RepresentationObject(RepresentationObject rObject) {
		this(rObject, rObject.getRootFile(), rObject.getPartFiles());
	}

	/**
	 * Constructs a new {@link RepresentationObject} with the given parameters.
	 * 
	 * @param rodaObject
	 * @param status
	 * @param descriptionObjectID
	 * @param rootFile
	 * @param partFiles
	 */
	public RepresentationObject(RODAObject rodaObject, String status,
			String descriptionObjectID, RepresentationFile rootFile,
			RepresentationFile[] partFiles) {
		this(rodaObject, new String[] { status }, descriptionObjectID,
				rootFile, partFiles);
	}

	/**
	 * Constructs a {@link RepresentationObject} with the given arguments.
	 * 
	 * @param object
	 * @param statuses
	 * @param descriptionObjectID
	 * @param rootFile
	 * @param partFiles
	 */
	public RepresentationObject(RODAObject object, String[] statuses,
			String descriptionObjectID, RepresentationFile rootFile,
			RepresentationFile[] partFiles) {
		super(object, statuses, descriptionObjectID);
		setRootFile(rootFile);
		setPartFiles(partFiles);
	}

	/**
	 * Constructs a {@link RepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param label
	 * @param type
	 * @param subType
	 * @param statuses
	 * @param descriptionObjectID
	 * @param rootFile
	 * @param partFiles
	 */
	public RepresentationObject(String id, String label, String type,
			String subType, String[] statuses, String descriptionObjectID,
			RepresentationFile rootFile, RepresentationFile[] partFiles) {
		this(id, label, type, subType, null, null, null, statuses,
				descriptionObjectID, rootFile, partFiles);
	}

	/**
	 * Constructs a {@link RepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param label
	 * @param type
	 * @param subType
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param rootFile
	 * @param partFiles
	 *            import pt.gov.dgarq.roda.core.common.InvalidStateException;
	 *            import pt.gov.dgarq.roda.core.common.WrongCModelException;
	 * @param statuses
	 * @param descriptionObjectID
	 */
	public RepresentationObject(String id, String label, String type,
			String subType, Date lastModifiedDate, Date createdDate,
			String state, String[] statuses, String descriptionObjectID,
			RepresentationFile rootFile, RepresentationFile[] partFiles) {

		super(id, label, type, subType, lastModifiedDate, createdDate, state,
				statuses, descriptionObjectID);

		setRootFile(rootFile);
		setPartFiles(partFiles);
	}

	/**
	 * Constructs a {@link RepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param id
	 * @param type
	 * @param subType
	 * @param rootFile
	 * @param partFiles
	 * @param statuses
	 * @param descriptionObjectID
	 * @deprecated use
	 *             {@link #RepresentationObject(String, String, String, String, String[], String, RepresentationFile, RepresentationFile[])}
	 *             instead.
	 */
	@Deprecated
	public RepresentationObject(String id, String label, String type,
			String subType, RepresentationFile rootFile,
			RepresentationFile[] partFiles, String[] statuses,
			String descriptionObjectID) {
		this(id, label, type, subType, null, null, null, statuses,
				descriptionObjectID, rootFile, partFiles);
	}

	/**
	 * Constructs a new {@link RepresentationObject} with the given parameters.
	 * 
	 * @param rodaObject
	 * @param rootFile
	 * @param partFiles
	 * @param status
	 * @param descriptionObjectID
	 * @deprecated use
	 *             {@link #RepresentationObject(RODAObject, String, String, RepresentationFile, RepresentationFile[])}
	 *             instead.
	 */
	@Deprecated
	public RepresentationObject(RODAObject rodaObject,
			RepresentationFile rootFile, RepresentationFile[] partFiles,
			String status, String descriptionObjectID) {
		this(rodaObject, rootFile, partFiles, new String[] { status },
				descriptionObjectID);
	}

	/**
	 * Constructs a {@link RepresentationObject} with the given arguments.
	 * 
	 * @param object
	 * @param rootFile
	 * @param partFiles
	 * @param statuses
	 * @param descriptionObjectID
	 * @deprecated use
	 *             {@link #RepresentationObject(RODAObject, String[], String, RepresentationFile, RepresentationFile[])}
	 *             instead.
	 */
	@Deprecated
	public RepresentationObject(RODAObject object, RepresentationFile rootFile,
			RepresentationFile[] partFiles, String[] statuses,
			String descriptionObjectID) {
		super(object, statuses, descriptionObjectID);
		setRootFile(rootFile);
		setPartFiles(partFiles);
	}

	/**
	 * @see RODAObject#toString()
	 */
	@Override
	public String toString() {

		int partFilesCount = (getPartFiles() != null) ? getPartFiles().length
				: 0;

		return "RepresentationObject( " + super.toString() + ", rootFile="
				+ getRootFile() + ", partFiles=" + partFilesCount + " )";
	}

	/**
	 * @return the rootFile
	 */
	public RepresentationFile getRootFile() {
		return rootFile;
	}

	/**
	 * @param rootFile
	 *            the rootFile to set
	 */
	public void setRootFile(RepresentationFile rootFile) {
		if (rootFile == null) {
			throw new NullPointerException("root file cannot be null");
		}
		this.rootFile = rootFile;
	}

	/**
	 * @return the partFiles
	 */
	public RepresentationFile[] getPartFiles() {
		return partFiles
				.toArray(new RepresentationFile[partFiles.size()]);
	}

	/**
	 * @param partFiles
	 *            the partFiles to set
	 */
	public void setPartFiles(RepresentationFile[] partFiles) {
		this.partFiles.clear();
		if (partFiles != null) {
			this.partFiles.addAll(Arrays.asList(partFiles));
		}
	}

	/**
	 * @param partFile
	 *            the partFile to add
	 */
	public void addPartFile(RepresentationFile partFile) {
		if (partFile != null) {
			this.partFiles.add(partFile);
		}
	}

}
