/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.upload;

import pt.gov.dgarq.roda.sipcreator.SIP;

/**
 * Extending SIP to add a checked flag
 * 
 * @author Luis Faria
 */
public class CheckedSIP {
	private boolean checked;
	private SIP sip;

	/**
	 * Create a checked SIP
	 * 
	 * @param sip
	 */
	public CheckedSIP(SIP sip) {
		checked = true;
		this.sip = sip;
	}

	/**
	 * Is SIP checked
	 * 
	 * @return true if checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * Set SIP checked
	 * 
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * Get the SIP
	 * 
	 * @return the SIP
	 */
	public SIP getSip() {
		return sip;
	}

}
