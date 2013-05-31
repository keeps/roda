package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.InputStream;

import pt.gov.dgarq.roda.core.data.RepresentationFile;

/**
 * This is a representation file that has an {@link InputStream} instead of an
 * access URL.
 * 
 * @author Rui Castro
 */
public class StreamRepresentationFile extends RepresentationFile {
	private static final long serialVersionUID = -2128160002514927757L;

	private InputStream inputStream = null;

	/**
	 * Constructs an empty {@link StreamRepresentationFile}.
	 */
	public StreamRepresentationFile() {
	}

	/**
	 * Constructs a {@link StreamRepresentationFile} with the given parameters.
	 * 
	 * @param id
	 *            the file unique identification inside the
	 *            {@link StreamRepresentationObject}.
	 * @param originalName
	 *            the file's original name.
	 * @param mimetype
	 *            the file's mimetype.
	 * @param size
	 *            the file's size in bytes.
	 * @param inputStream
	 *            the {@link InputStream} to access this file contents.
	 */
	public StreamRepresentationFile(String id, String originalName,
			String mimetype, long size, InputStream inputStream) {
		super(id, originalName, mimetype, size, null);
		setInputStream(inputStream);
	}

	/**
	 * Construct a {@link StreamRepresentationFile} cloning an existing
	 * {@link StreamRepresentationFile}.
	 * 
	 * @param representationFile
	 *            the {@link StreamRepresentationFile} to clone.
	 */
	public StreamRepresentationFile(StreamRepresentationFile representationFile) {
		this(representationFile.getId(), representationFile.getOriginalName(),
				representationFile.getMimetype(), representationFile.getSize(),
				representationFile.getInputStream());
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "StreamRepresentationFile( " + super.toString()
				+ ", inputStream=" + getInputStream() + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof StreamRepresentationFile) {
			StreamRepresentationFile other = (StreamRepresentationFile) obj;
			return super.equals(other)
					&& getInputStream().equals(other.getInputStream());
		} else {
			return false;
		}
	}

	/**
	 * @see RepresentationFile#setAccessURL(String)
	 */
	@Override
	public void setAccessURL(String accessURL) {
	}

	/**
	 * @see RepresentationFile#getAccessURL()
	 */
	@Override
	public String getAccessURL() {
		return null;
	}

	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @param inputStream
	 *            the inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
