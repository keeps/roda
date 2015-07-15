package org.roda.legacy.aip.data;

import java.io.Serializable;

import pt.gov.dgarq.roda.core.data.v2.RODAObject;

/**
 * @author Rui Castro
 */
public class Fixity implements Serializable {
	private static final long serialVersionUID = 5643738632324867731L;

	private String messageDigestAlgorithm = null;
	private String messageDigest = null;
	private String messageDigestOriginator = null;

	/**
	 * Constructs a new {@link Fixity}.
	 */
	public Fixity() {
	}

	/**
	 * Constructs a new {@link Fixity} cloning an existing {@link Fixity}.
	 * 
	 * @param fixity
	 */
	private Fixity(Fixity fixity) {
		this(fixity.getMessageDigest(), fixity.getMessageDigestAlgorithm(),
				fixity.getMessageDigestOriginator());
	}

	/**
	 * Constructs a new {@link Fixity} with the given parameters.
	 * 
	 * @param messageDigestAlgorithm
	 * @param messageDigest
	 * @param messageDigestOriginator
	 * 
	 */
	public Fixity(String messageDigestAlgorithm, String messageDigest,
			String messageDigestOriginator) {
		setMessageDigestAlgorithm(messageDigestAlgorithm);
		setMessageDigest(messageDigest);
		setMessageDigestOriginator(messageDigestOriginator);
	}

	@Override
	protected Object clone() {
		return new Fixity(this);
	}

	/**
	 * @param obj
	 * 
	 * @return <code>true</code> if the objects are equal and <code>false</code>
	 *         otherwise.
	 * 
	 * @see RODAObject#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Fixity) {
			Fixity other = (Fixity) obj;

			return (getMessageDigestAlgorithm() == other
					.getMessageDigestAlgorithm() || getMessageDigestAlgorithm()
					.equals(other.getMessageDigestAlgorithm()))
					&& (getMessageDigest() == other.getMessageDigest() || getMessageDigest()
							.equals(getMessageDigest()));
		} else {
			return false;
		}
	}

	/**
	 * @return a {@link String} with this object's info.
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "Fixity(messageDigestAlgorithm=" + getMessageDigestAlgorithm()
				+ ", messageDigest" + getMessageDigest()
				+ ", messageDigestOriginator" + getMessageDigestOriginator()
				+ ")";
	}

	/**
	 * @return the messageDigestAlgorithm
	 */
	public String getMessageDigestAlgorithm() {
		return messageDigestAlgorithm;
	}

	/**
	 * @param messageDigestAlgorithm
	 *            the messageDigestAlgorithm to set
	 */
	public void setMessageDigestAlgorithm(String messageDigestAlgorithm) {
		this.messageDigestAlgorithm = messageDigestAlgorithm;
	}

	/**
	 * @return the messageDigest
	 */
	public String getMessageDigest() {
		return messageDigest;
	}

	/**
	 * @param messageDigest
	 *            the messageDigest to set
	 */
	public void setMessageDigest(String messageDigest) {
		this.messageDigest = messageDigest;
	}

	/**
	 * @return the messageDigestOriginator
	 */
	public String getMessageDigestOriginator() {
		return messageDigestOriginator;
	}

	/**
	 * @param messageDigestOriginator
	 *            the messageDigestOriginator to set
	 */
	public void setMessageDigestOriginator(String messageDigestOriginator) {
		this.messageDigestOriginator = messageDigestOriginator;
	}
}
