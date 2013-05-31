package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.util.Date;

import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;

/**
 * @author Rui Castro
 * 
 */
public class SIPEventPreservationObject extends EventPreservationObject {
	private static final long serialVersionUID = -5403611658667525339L;

	private File premisFile = null;

	private SIPRepresentationPreservationObject target = null;

	private SIPAgentPreservationObject agent = null;

	/**
	 * Constructs a new {@link SIPEventPreservationObject}.
	 */
	public SIPEventPreservationObject() {
	}

	/**
	 * Constructs a new {@link SIPEventPreservationObject}.
	 * 
	 * @param event
	 */
	public SIPEventPreservationObject(EventPreservationObject event) {
		super(event);
	}

	/**
	 * Constructs a new {@link SIPEventPreservationObject}.
	 * 
	 * @param pid
	 * @param label
	 * @param model
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 */
	public SIPEventPreservationObject(String pid, String label, String model,
			Date lastModifiedDate, Date createdDate, String state) {
		super(pid, label, model, lastModifiedDate, createdDate, state);
	}

	/**
	 * @see EventPreservationObject#toString()
	 */
	@Override
	public String toString() {
		return "SIPEventPreservationObject( premisFile=" + getPremisFile() //$NON-NLS-1$
				+ ", " + super.toString() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see RODAObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SIPEventPreservationObject) {
			SIPEventPreservationObject other = (SIPEventPreservationObject) obj;

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
	 * @return the target
	 */
	public SIPRepresentationPreservationObject getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(SIPRepresentationPreservationObject target) {
		this.target = target;
	}

	/**
	 * @return the agent
	 */
	public SIPAgentPreservationObject getAgent() {
		return agent;
	}

	/**
	 * @param agent
	 *            the agent to set
	 */
	public void setAgent(SIPAgentPreservationObject agent) {
		this.agent = agent;
	}

}
