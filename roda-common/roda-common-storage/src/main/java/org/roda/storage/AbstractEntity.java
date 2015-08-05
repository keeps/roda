package org.roda.storage;

import java.util.Map;
import java.util.Set;

public class AbstractEntity implements Entity {

	private StoragePath storagePath;

	private Map<String, Set<String>> metadata;

	public AbstractEntity(StoragePath storagePath,
			Map<String, Set<String>> metadata) {
		super();
		this.storagePath = storagePath;
		this.metadata = metadata;
	}

	/**
	 * @return the storagePath
	 */
	@Override
	public StoragePath getStoragePath() {
		return storagePath;
	}

	/**
	 * @param storagePath
	 *            the storagePath to set
	 */
	public void setStoragePath(StoragePath storagePath) {
		this.storagePath = storagePath;
	}

	/**
	 * @return the metadata
	 */
	@Override
	public Map<String, Set<String>> getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata
	 *            the metadata to set
	 */
	public void setMetadata(Map<String, Set<String>> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result
				+ ((storagePath == null) ? 0 : storagePath.hashCode());
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
		AbstractEntity other = (AbstractEntity) obj;
		if (metadata == null) {
			if (other.metadata != null) {
				return false;
			}
		} else if (!metadata.equals(other.metadata)) {
			return false;
		}
		if (storagePath == null) {
			if (other.storagePath != null) {
				return false;
			}
		} else if (!storagePath.equals(other.storagePath)) {
			return false;
		}
		return true;
	}

}
