package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;

/**
 * This is a file inside a {@link RepresentationObject}.
 * 
 * @author Rui Castro
 */
public class RepresentationFile implements Serializable {
	private static final long serialVersionUID = 542428357688741774L;

	private String id = null;
	private String originalName = null;
	private String mimetype = null;
	private long size = 0;
	private String accessURL = null;

	/**
	 * Constructs an empty {@link RepresentationFile}.
	 */
	public RepresentationFile() {
	}

	/**
	 * Constructs a {@link RepresentationFile} with the given parameters.
	 * 
	 * @param id
	 *            the file unique identification inside the
	 *            {@link RepresentationObject}.
	 * @param originalName
	 *            the file's original name.
	 * @param mimetype
	 *            the file's mimetype.
	 * @param size
	 *            the file's size in bytes.
	 * @param accessURL
	 *            the relative URL to access this file.
	 */
	public RepresentationFile(String id, String originalName, String mimetype,
			long size, String accessURL) {
		setId(id);
		setOriginalName(originalName);
		setMimetype(mimetype);
		setSize(size);
		setAccessURL(accessURL);
	}

	/**
	 * Construct a {@link RepresentationFile} cloning an existing
	 * {@link RepresentationFile}.
	 * 
	 * @param representationFile
	 *            the {@link RepresentationFile} to clone.
	 */
	public RepresentationFile(RepresentationFile representationFile) {
		this(representationFile.getId(), representationFile.getOriginalName(),
				representationFile.getMimetype(), representationFile.getSize(),
				representationFile.getAccessURL());
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "RepresentationFile(id=" + getId() + ", originalName="
				+ getOriginalName() + ", mimetype=" + getMimetype() + ", size="
				+ getSize() + ", accessURL=" + getAccessURL() + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
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
	 *            the id to set
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
	 *            the originalName to set
	 */
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	/**
	 * @return the mimetype
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * @param mimetype
	 *            the mimetype to set
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
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
	 *            the accessURL to set
	 */
	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

}
