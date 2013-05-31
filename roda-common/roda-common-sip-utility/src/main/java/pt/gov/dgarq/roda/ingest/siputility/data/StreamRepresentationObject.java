package pt.gov.dgarq.roda.ingest.siputility.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * @author Rui Castro
 */
public class StreamRepresentationObject extends RepresentationObject {
	private static final long serialVersionUID = 1499346553061657308L;

	private static final Logger logger = Logger
			.getLogger(StreamRepresentationObject.class);

	private StreamRepresentationFile rootStream = null;
	private List<StreamRepresentationFile> partStreams = new ArrayList<StreamRepresentationFile>();

	/**
	 * Constructs an empty {@link StreamRepresentationObject}.
	 */
	public StreamRepresentationObject() {
	}

	/**
	 * Constructs a new {@link StreamRepresentationObject} cloning an existing
	 * {@link StreamRepresentationObject}.
	 * 
	 * @param rObject
	 *            a Representation Object.
	 */
	public StreamRepresentationObject(StreamRepresentationObject rObject) {
		super(rObject);
		setRootStream(rObject.getRootStream());
		setPartStreams(rObject.getPartStreams());
	}

	/**
	 * Constructs a {@link StreamRepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param type
	 * @param rootStream
	 * @param partStreams
	 * @param statuses
	 * @param descriptionObjectPID
	 */
	public StreamRepresentationObject(String id, String type,
			StreamRepresentationFile rootStream,
			StreamRepresentationFile[] partStreams, String[] statuses,
			String descriptionObjectPID) {
		this(id, type, null, rootStream, partStreams, statuses,
				descriptionObjectPID);
	}

	/**
	 * Constructs a {@link StreamRepresentationObject} with the given arguments.
	 * 
	 * @param id
	 * @param type
	 * @param subType
	 * @param rootStream
	 * @param partStreams
	 * @param statuses
	 * @param descriptionObjectPID
	 */
	public StreamRepresentationObject(String id, String type, String subType,
			StreamRepresentationFile rootStream,
			StreamRepresentationFile[] partStreams, String[] statuses,
			String descriptionObjectPID) {

		super(null, id, type, subType, rootStream, partStreams, statuses,
				descriptionObjectPID);

		setRootStream(rootStream);
		setPartStreams(partStreams);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "StreamRepresentationObject(" + super.toString() + ")";

	}

	/**
	 * @return the rootStream
	 */
	public RepresentationFile getRootFile() {
		return getRootStream();
	}

	/**
	 * @see RepresentationObject#setRootFile(RepresentationFile)
	 */
	@Override
	public void setRootFile(RepresentationFile rootFile) {
		logger
				.warn("This method should not be called. Call setRootSteam(StreamRepresentationFile) instead.");
		throw new UnsupportedOperationException(
				"This method should not be called. Call setRootSteam(StreamRepresentationFile) instead.");
		// super.setRootFile(rootFile);
	}

	/**
	 * @return the partStreams
	 */
	public RepresentationFile[] getPartFiles() {
		return getPartStreams();
	}

	/**
	 * @see RepresentationObject#setPartFiles(RepresentationFile[])
	 */
	@Override
	public void setPartFiles(RepresentationFile[] partFiles) {
		logger
				.warn("This method should not be called. Call setPartSteams(StreamRepresentationFile[]) instead.");
		throw new UnsupportedOperationException(
				"This method should not be called. Call setPartSteams(StreamRepresentationFile[]) instead.");
		// super.setPartFiles(partFiles);
	}

	/**
	 * @return the rootStream
	 */
	public StreamRepresentationFile getRootStream() {
		return this.rootStream;
	}

	/**
	 * @param rootStream
	 *            the rootStream to set
	 */
	public void setRootStream(StreamRepresentationFile rootStream) {
		this.rootStream = rootStream;
	}

	/**
	 * @return the partStreams
	 */
	public StreamRepresentationFile[] getPartStreams() {
		return this.partStreams
				.toArray(new StreamRepresentationFile[this.partStreams.size()]);
	}

	/**
	 * @param partStreams
	 *            the partStreams to set
	 */
	public void setPartStreams(List<StreamRepresentationFile> partStreams) {
		this.partStreams.clear();
		if (partStreams != null) {
			this.partStreams.addAll(partStreams);
		}
	}

	/**
	 * @param partStreams
	 *            the partStreams to set
	 */
	public void setPartStreams(StreamRepresentationFile... partStreams) {
		setPartFiles(partStreams);
	}

}
