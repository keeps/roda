package org.roda.storage;

import java.util.Map;
import java.util.Set;

public class AbstractResource extends AbstractEntity implements Resource {

	private boolean directory;

	public AbstractResource(StoragePath storagePath,
			Map<String, Set<String>> metadata, boolean directory) {
		super(storagePath, metadata);
		this.directory = directory;
	}

	/**
	 * @return the directory
	 */
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * @param directory
	 *            the directory to set
	 */
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (directory ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractResource other = (AbstractResource) obj;
		if (directory != other.directory) {
			return false;
		}
		return true;
	}

}
