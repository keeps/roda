package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;

/**
 * @author Rui Castro
 */
public class SIP implements DataChangeListener {
	private static final Logger logger = Logger.getLogger(SIP.class);

	private Set<DataChangeListener> dataChangeListeners = new HashSet<DataChangeListener>();

	private File directory = null;

	private String parentPID = null;

	private SIPDescriptionObject descriptionObject = null;

	/**
	 * Constructs a new {@link SIP} with and empty {@link SIPDescriptionObject}
	 * and a {@link SIPRepresentationObject} with type
	 * {@link SIPRepresentationObject#UNKNOWN} and status
	 * {@link SIPRepresentationObject#STATUS_ORIGINAL}.
	 */
	public SIP() {
		setDescriptionObject(new SIPDescriptionObject());
	}

	/**
	 * Constructs a new {@link SIP} with specified parameters.
	 * 
	 * @param parentPID
	 *            the PID of the parent {@link DescriptionObject}.
	 * @param sipDO
	 *            the {@link SIPDescriptionObject}.
	 */
	public SIP(String parentPID, SIPDescriptionObject sipDO) {
		setParentPID(parentPID);
		setDescriptionObject(sipDO);
	}

	/**
	 * Constructs a new {@link SIP} cloning and existing one.
	 * 
	 * @param sip
	 *            the {@link SIP} to clone.
	 */
	public SIP(SIP sip) {
		setDirectory(sip.getDirectory());
		setParentPID(sip.getParentPID());
		setDescriptionObject(sip.getDescriptionObject());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (directory=" + getDirectory() //$NON-NLS-1$
				+ ", parentPID=" + getParentPID() + ", descriptionObject=" //$NON-NLS-1$ //$NON-NLS-2$
				+ getDescriptionObject() + ")"; //$NON-NLS-1$
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

		for (SIPDescriptionObject sipDO : getDescriptionObjects()) {
			sipDO.setFile(new File(directory, sipDO.getFile().getName()));
		}

		for (SIPRepresentationObject sipRO : SIPUtility
				.getRepresentationObjects(this)) {
			if (sipRO.getDirectory() != null) {
				sipRO.setDirectory(new File(directory, sipRO.getDirectory()
						.getName()));
			}
		}
	}

	/**
	 * @return the parentPID
	 */
	public String getParentPID() {
		return parentPID;
	}

	/**
	 * @param parentPID
	 *            the parentPID to set
	 */
	public void setParentPID(String parentPID) {
		this.parentPID = parentPID;
		fireDataChangedEvent();
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

		if (this.descriptionObject != null
				&& this.descriptionObject != descriptionObject) {
			this.descriptionObject.removeChangeListener(this);
		}

		this.descriptionObject = descriptionObject;

		if (this.descriptionObject != null) {

			this.descriptionObject.addChangeListener(this);

		}
	}

	/**
	 * Returns all {@link SIPDescriptionObject}s inside this {@link SIP}.
	 * 
	 * @return a {@link List} of {@link SIPDescriptionObject}.
	 */
	public List<SIPDescriptionObject> getDescriptionObjects() {
		return SIPUtility.getDescriptionObjects(getDescriptionObject());
	}

	/**
	 * Returns all {@link SIPRepresentationObject}s inside this {@link SIP}.
	 * 
	 * @return a {@link List} of {@link SIPRepresentationObject}.
	 */
	public List<SIPRepresentationObject> getRepresentations() {
		return SIPUtility.getRepresentationObjects(this);
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
	 * This method is called when the {@link SIPDescriptionObject} or the
	 * {@link SIPRepresentationObject} had changed.
	 * 
	 * @param evtDataChanged
	 */
	public void dataChanged(DataChangedEvent evtDataChanged) {
		if (evtDataChanged.getSource() instanceof SIPDescriptionObject) {
			if (evtDataChanged.getSource().equals(getDescriptionObject())) {
				fireDataChangedEvent(evtDataChanged);
			} else {
				logger
						.debug("DescriptionObject has changed, but is not mine. Why am I listening to this???"); //$NON-NLS-1$
			}
		} else {
			logger.warn("dataChanged called but event source is " //$NON-NLS-1$
					+ evtDataChanged.getSource());
		}
	}

	private void fireDataChangedEvent() {
		fireDataChangedEvent(null);
	}

	synchronized private void fireDataChangedEvent(EventObject causeEvent) {
		DataChangedEvent event = new DataChangedEvent(this, causeEvent);
		for (DataChangeListener listener : dataChangeListeners) {
			listener.dataChanged(event);
		}
	}

}
