package pt.gov.dgarq.roda.plugins.converters.common;

import java.io.File;

import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * @author Rui Castro
 */
public class LocalRepresentationObject extends RepresentationObject {
	private static final long serialVersionUID = 1213107386511485144L;

	private File directory = null;

	/**
	 * Constructs a new {@link LocalRepresentationObject}.
	 */
	public LocalRepresentationObject() {
		setDirectory(new File(".")); //$NON-NLS-1$
	}

	/**
	 * Constructs a new {@link LocalRepresentationObject}.
	 * 
	 * @param directory
	 */
	public LocalRepresentationObject(File directory) {
		setDirectory(directory);
	}

	/**
	 * @param directory
	 * @param representation
	 */
	public LocalRepresentationObject(File directory,
			RepresentationObject representation) {
		super(representation);
		setDirectory(directory);
	}

	/**
	 * @param representation
	 */
	public LocalRepresentationObject(LocalRepresentationObject representation) {
		super(representation);
		setDirectory(representation.getDirectory());
	}

	/**
	 * @see RepresentationObject#toString()
	 */
	public String toString() {
		return "LocalRepresentationObject( " + super.toString() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * @param directory
	 *            the directory to set
	 * 
	 * @throws IllegalArgumentException
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

}
