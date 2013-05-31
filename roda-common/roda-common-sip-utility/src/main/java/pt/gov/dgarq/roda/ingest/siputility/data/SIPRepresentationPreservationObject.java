package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;

/**
 * @author Rui Castro
 */
public class SIPRepresentationPreservationObject extends
		RepresentationPreservationObject {

	private static final long serialVersionUID = 8530043282857292068L;

	private File premisFile = null;

	private SIPRepresentationObject representationObject = null;

	private List<SIPEventPreservationObject> preservationEvents = new ArrayList<SIPEventPreservationObject>();

	private SIPEventPreservationObject derivationEvent = null;
	private SIPRepresentationPreservationObject derivedFromRepresentationObject = null;

	/**
	 * Constructs a new {@link SIPRepresentationPreservationObject}.
	 */
	public SIPRepresentationPreservationObject() {
	}

	/**
	 * @param representationPreservationObject
	 */
	public SIPRepresentationPreservationObject(
			RepresentationPreservationObject representationPreservationObject) {
		super(representationPreservationObject);
	}

	/**
	 * @param pid
	 * @param label
	 * @param model
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param ID
	 */
	public SIPRepresentationPreservationObject(String pid, String label,
			String model, Date lastModifiedDate, Date createdDate,
			String state, String ID) {
		super(pid, label, model, lastModifiedDate, createdDate, state, ID);
	}

	/**
	 * @see RepresentationPreservationObject#toString()
	 */
	@Override
	public String toString() {
		return "SIPRepresentationPreservationObject( premisFile=" //$NON-NLS-1$
				+ getPremisFile() + ", " + super.toString() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see RODAObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SIPRepresentationPreservationObject) {
			SIPRepresentationPreservationObject other = (SIPRepresentationPreservationObject) obj;

			if (getPremisFile() == null) {
				return super.equals(other);
			} else {
				return getPremisFile().equals(other.getPremisFile())
						&& super.equals(other);
			}

		} else {
			return false;
		}
	}

	/**
	 * @return the premisFile
	 */
	public File getPremisFile() {
		return premisFile;
	}

	/**
	 * @param premisFile
	 *            the premisFile to set
	 */
	public void setPremisFile(File premisFile) {
		this.premisFile = premisFile;
	}

	/**
	 * @return the preservationEvents
	 */
	public List<SIPEventPreservationObject> getPreservationEvents() {
		return new ArrayList<SIPEventPreservationObject>(preservationEvents);
	}

	/**
	 * @param preservationEvents
	 */
	public void setPreservationEvents(
			List<SIPEventPreservationObject> preservationEvents) {

		this.preservationEvents.clear();

		if (preservationEvents != null) {
			for (SIPEventPreservationObject preservationEvent : preservationEvents) {
				preservationEvent.setTarget(this);
				this.preservationEvents.add(preservationEvent);
			}
			// this.preservationEvents.addAll(preservationEvents);
		}
	}

	/**
	 * Adds a {@link SIPEventPreservationObject}.
	 * 
	 * @param preservationEvent
	 */
	public void addPreservationEvent(
			SIPEventPreservationObject preservationEvent) {

		if (preservationEvent != null
				&& !this.preservationEvents.contains(preservationEvent)) {

			preservationEvent.setTarget(this);
			this.preservationEvents.add(preservationEvent);

		}
	}

	/**
	 * @param preservationEvent
	 */
	public void removePreservationEvent(
			SIPEventPreservationObject preservationEvent) {

		if (preservationEvent != null
				&& this.preservationEvents.contains(preservationEvent)) {

			preservationEvent.setTarget(null);
			this.preservationEvents.remove(preservationEvent);

		}
	}

	/**
	 * @return the derivationEvent
	 */
	public SIPEventPreservationObject getDerivationEvent() {
		return derivationEvent;
	}

	/**
	 * @param derivationEvent
	 *            the derivationEvent to set
	 */
	public void setDerivationEvent(SIPEventPreservationObject derivationEvent) {
		this.derivationEvent = derivationEvent;
	}

	/**
	 * @return the derivedFromRepresentationObject
	 */
	public SIPRepresentationPreservationObject getDerivedFromRepresentationObject() {
		return derivedFromRepresentationObject;
	}

	/**
	 * @param derivedFromRepresentationObject
	 *            the derivedFromRepresentationObject to set
	 */
	public void setDerivedFromRepresentationObject(
			SIPRepresentationPreservationObject derivedFromRepresentationObject) {
		this.derivedFromRepresentationObject = derivedFromRepresentationObject;
	}

	/**
	 * @return the representationObject
	 */
	public SIPRepresentationObject getRepresentationObject() {
		return representationObject;
	}

	/**
	 * @param representationObject
	 *            the representationObject to set
	 */
	public void setRepresentationObject(
			SIPRepresentationObject representationObject) {
		this.representationObject = representationObject;
	}

}
