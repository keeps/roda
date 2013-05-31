package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import java.util.EventObject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class SIPRepresentationObject extends RepresentationObject {
	private static final long serialVersionUID = 5001620383599433457L;

	private static final Logger logger = Logger
			.getLogger(SIPRepresentationObject.class);

	private Set<DataChangeListener> dataChangeListeners = new LinkedHashSet<DataChangeListener>();

	private File directory = null;

	private SIPDescriptionObject descriptionObject = null;

	private SIPRepresentationPreservationObject preservationObject = null;

	/**
	 * Constructs a new {@link SIPRepresentationObject}.
	 */
	public SIPRepresentationObject() {
	}

	/**
	 * Constructs a new {@link SIPRepresentationObject}.
	 * 
	 * @param type
	 * @param subType
	 * @param status
	 */
	public SIPRepresentationObject(String type, String subType, String status) {
		setId(RepresentationBuilder.createNewRepresentationID());
		setType(type);
		setSubType(subType);
		setStatuses(new String[] { status });
	}

	/**
	 * Constructs a new {@link SIPRepresentationObject} from an existing
	 * {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            the {@link RepresentationObject}.
	 */
	public SIPRepresentationObject(RepresentationObject rObject) {
		super(rObject);
	}

	/**
	 * @see RepresentationObject#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "( directory=" + getDirectory()
				+ ", " + super.toString() + ")";
	}

	/**
	 * @see RODAObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SIPRepresentationObject) {
			SIPRepresentationObject other = (SIPRepresentationObject) obj;
			return getId() == other.getId() || getId().equals(other.getId());
		} else {
			return false;
		}
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
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
		try {
			getRootFile().setAccessURL(
					mapDirectory(directory, getRootFile().getAccessURL()));
			if (getPartFiles() != null) {
				for (RepresentationFile partFile : getPartFiles()) {
					partFile.setAccessURL(mapDirectory(directory, partFile
							.getAccessURL()));
				}
			}
		} catch (MalformedURLException e) {
			logger.error("Errro setting directory", e);
		}
	}

	private String mapDirectory(File directory, String url)
			throws MalformedURLException {
		return new File(directory, new File(URI.create(url)).getName()).toURI()
				.toURL().toString();
	}

	@Override
	public void setDescriptionObjectPID(String descriptionObjectPID) {
		super.setDescriptionObjectPID(descriptionObjectPID);
		fireDataChangedEvent();
	}

	@Override
	public void setPid(String pid) {
		super.setPid(pid);
		fireDataChangedEvent();
	}

	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		fireDataChangedEvent();
	}

	@Override
	public void setContentModel(String contentModel) {
		super.setContentModel(contentModel);
		fireDataChangedEvent();
	}

	@Override
	public void setLastModifiedDate(Date lastModifiedDate) {
		super.setLastModifiedDate(lastModifiedDate);
		fireDataChangedEvent();
	}

	@Override
	public void setCreatedDate(Date createdDate) {
		super.setCreatedDate(createdDate);
		fireDataChangedEvent();
	}

	@Override
	public void setState(String state) {
		super.setState(state);
		fireDataChangedEvent();
	}

	@Override
	public void setStatuses(String[] statuses) throws NullPointerException,
			IllegalArgumentException {
		super.setStatuses(statuses);
		fireDataChangedEvent();
	}

	@Override
	public void setType(String type) {
		super.setType(type);
		fireDataChangedEvent();
	}

	@Override
	public void setRootFile(RepresentationFile rootFile) {
		super.setRootFile(rootFile);
		fireDataChangedEvent();
	}

	@Override
	public void setPartFiles(RepresentationFile[] partFiles) {
		super.setPartFiles(partFiles);
		fireDataChangedEvent();
	}

	/**
	 * @param listener
	 */
	synchronized public void addChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	synchronized public void removeChangeListener(DataChangeListener listener) {
		dataChangeListeners.remove(listener);
	}

	/**
	 * @return the descriptionObject
	 */
	public SIPDescriptionObject getDescriptionObject() {
		return descriptionObject;
	}

	/**
	 * @param descriptionObject
	 *            the descriptionObject to set
	 */
	public void setDescriptionObject(SIPDescriptionObject descriptionObject) {
		this.descriptionObject = descriptionObject;
	}

	/**
	 * @return the preservationObject
	 */
	public SIPRepresentationPreservationObject getPreservationObject() {
		return preservationObject;
	}

	/**
	 * @param preservationObject
	 *            the preservationObject to set
	 */
	public void setPreservationObject(
			SIPRepresentationPreservationObject preservationObject) {

		this.preservationObject = preservationObject;

		if (this.preservationObject != null) {
			this.preservationObject.setRepresentationObject(this);
		}
	}

	private void fireDataChangedEvent() {
		fireDataChangedEvent(null);
	}

	synchronized private void fireDataChangedEvent(EventObject causeEvent) {

		DataChangedEvent event = new DataChangedEvent(this, causeEvent);

		if (dataChangeListeners != null) {
			for (DataChangeListener listener : dataChangeListeners) {
				listener.dataChanged(event);
			}
		}
	}

}
