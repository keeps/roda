package org.roda.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class Representation {

	private final String id;
	private final String aipId;
	private final boolean active;
	private final Date dateCreated;
	private final Date dateModified;
	private final Set<RepresentationState> statuses;
	private final String type;
	private final List<String> fileIds;

	public Representation(String id, String aipId, boolean active,
			Date dateCreated, Date dateModified,
			Set<RepresentationState> statuses, String type, List<String> fileIds) {
		super();
		this.id = id;
		this.aipId = aipId;
		this.active = active;
		this.dateCreated = dateCreated;
		this.dateModified = dateModified;
		this.statuses = statuses;
		this.type = type;
		this.fileIds = fileIds;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the aipId
	 */
	public String getAipId() {
		return aipId;
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

	/**
	 * @return the statuses
	 */
	public Set<RepresentationState> getStatuses() {
		return statuses;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the fileIds
	 */
	public List<String> getFileIds() {
		return fileIds;
	}

	@Override
	public String toString() {
		return "Representation [id=" + id + ", aipId=" + aipId + ", active="
				+ active + ", dateCreated=" + dateCreated + ", dateModified="
				+ dateModified + ", statuses=" + statuses + ", type=" + type
				+ ", fileIds=" + fileIds + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
		result = prime * result
				+ ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result
				+ ((dateModified == null) ? 0 : dateModified.hashCode());
		result = prime * result + ((fileIds == null) ? 0 : fileIds.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((statuses == null) ? 0 : statuses.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Representation other = (Representation) obj;
		if (active != other.active) {
			return false;
		}
		if (aipId == null) {
			if (other.aipId != null) {
				return false;
			}
		} else if (!aipId.equals(other.aipId)) {
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
		if (fileIds == null) {
			if (other.fileIds != null) {
				return false;
			}
		} else if (!fileIds.equals(other.fileIds)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (statuses == null) {
			if (other.statuses != null) {
				return false;
			}
		} else if (!statuses.equals(other.statuses)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
