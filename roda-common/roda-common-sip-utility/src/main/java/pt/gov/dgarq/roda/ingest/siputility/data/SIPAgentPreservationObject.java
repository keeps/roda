package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.util.Date;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;

/**
 * @author Rui Castro
 */
public class SIPAgentPreservationObject extends AgentPreservationObject {
	private static final long serialVersionUID = 2382795648392177582L;

	private File premisFile = null;

	/**
	 * Constructs a new {@link SIPAgentPreservationObject}.
	 */
	public SIPAgentPreservationObject() {
	}

	/**
	 * Constructs a new {@link SIPAgentPreservationObject}.
	 * 
	 * @param agent
	 */
	public SIPAgentPreservationObject(AgentPreservationObject agent) {
		super(agent);
	}

	/**
	 * @see AgentPreservationObject#toString()
	 */
	@Override
	public String toString() {
		return "SIPAgentPreservationObject( premisFile=" + getPremisFile() //$NON-NLS-1$
				+ ", " + super.toString() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see RODAObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SIPAgentPreservationObject) {
			SIPAgentPreservationObject other = (SIPAgentPreservationObject) obj;

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

}
