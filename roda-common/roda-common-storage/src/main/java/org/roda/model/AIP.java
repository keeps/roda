package org.roda.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AIP implements Serializable {

	private static final long serialVersionUID = 430629679119752757L;

	private String id;
	private String parentId;
	private boolean active;
	private Date dateCreated;
	private Date dateModified;
	private List<String> descriptiveMetadataIds;
	private List<String> representationIds;

	public AIP() {
		super();
	}

	public AIP(String id, String parentId, boolean active, Date dateCreated, Date dateModified,
			List<String> descriptiveMetadataIds, List<String> representationIds) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.active = active;
		this.dateCreated = dateCreated;
		this.dateModified = dateModified;
		this.descriptiveMetadataIds = descriptiveMetadataIds;
		this.representationIds = representationIds;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the identifier of the parent AIP or <code>null</code> if this AIP is
	 * on the top-level.
	 * 
	 * @return
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the dateModified
	 */
	public Date getDateModified() {
		return dateModified;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public void setDescriptiveMetadataIds(List<String> descriptiveMetadataIds) {
		this.descriptiveMetadataIds = descriptiveMetadataIds;
	}

	public void setRepresentationIds(List<String> representationIds) {
		this.representationIds = representationIds;
	}

	/**
	 * @return the descriptiveMetadataBinaryIds
	 */
	public List<String> getDescriptiveMetadataIds() {
		return descriptiveMetadataIds;
	}

	/**
	 * @return the representationIds
	 */
	public List<String> getRepresentationIds() {
		return representationIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result + ((dateModified == null) ? 0 : dateModified.hashCode());
		result = prime * result + ((descriptiveMetadataIds == null) ? 0 : descriptiveMetadataIds.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		result = prime * result + ((representationIds == null) ? 0 : representationIds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AIP other = (AIP) obj;
		if (active != other.active) {
			return false;
		}
		if (dateCreated == null) {
			if (other.dateCreated != null) {
				return false;
			}
		} else if (!dateCreated.equals(other.dateCreated)) {
			return false;
		}
		if (dateModified == null) {
			if (other.dateModified != null) {
				return false;
			}
		} else if (!dateModified.equals(other.dateModified)) {
			return false;
		}
		if (descriptiveMetadataIds == null) {
			if (other.descriptiveMetadataIds != null) {
				return false;
			}
		} else if (!descriptiveMetadataIds.equals(other.descriptiveMetadataIds)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (parentId == null) {
			if (other.parentId != null) {
				return false;
			}
		} else if (!parentId.equals(other.parentId)) {
			return false;
		}
		if (representationIds == null) {
			if (other.representationIds != null) {
				return false;
			}
		} else if (!representationIds.equals(other.representationIds)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AIP [id=" + id + ", parentId=" + parentId + ", active=" + active + ", dateCreated=" + dateCreated
				+ ", dateModified=" + dateModified + ", descriptiveMetadataIds=" + descriptiveMetadataIds
				+ ", representationIds=" + representationIds + "]";
	}

}
