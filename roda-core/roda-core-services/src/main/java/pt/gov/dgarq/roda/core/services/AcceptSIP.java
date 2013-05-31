package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;

import pt.gov.dgarq.roda.core.common.AcceptSIPException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.ingest.AcceptSIPTask;

/**
 * @author Rui Castro
 * 
 */
public class AcceptSIP extends RODAWebService {
	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(AcceptSIP.class);

	private FedoraClientUtility fedoraClientUtility = null;

	/**
	 * @throws RODAServiceException
	 */
	public AcceptSIP() throws RODAServiceException {
		super();

		String fedoraURL = getConfiguration().getString("fedoraURL");
		String fedoraGSearchURL = getConfiguration().getString(
				"fedoraGSearchURL");

		User clientUser = getClientUser();

		if (clientUser != null) {

			try {

				this.fedoraClientUtility = new FedoraClientUtility(fedoraURL,
						fedoraGSearchURL, getClientUser(),
						getClientUserPassword());

			} catch (FedoraClientException e) {
				throw new RODAServiceException(
						"Error creating Fedora client - " + e.getMessage(), e);
			} catch (MalformedURLException e) {
				throw new RODAServiceException("Bad URL for Fedora client - "
						+ e.getMessage(), e);
			}

		} else {

			throw new RODAServiceException(
					"User credentials are not available.");

		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Marks a {@link SIPState} as accepted of rejected.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState} to accept/reject.
	 * @param accept
	 *            <code>true</code> to accept, <code>false</code> to reject.
	 * @param reason
	 *            a message with the reason for accepting or rejecting the
	 *            {@link SIPState}.
	 * 
	 * @return the accepted/rejected {@link SIPState}.
	 * 
	 * @throws NoSuchSIPException
	 *             if the {@link SIPState} with ID given doesn't exist.
	 * @throws IllegalOperationException
	 *             if this task cannot be applied to {@link SIPState} with the
	 *             specified ID.
	 * @throws AcceptSIPException
	 *             if the action could not be performed.
	 */
	public SIPState acceptSIP(String sipID, boolean accept, String reason)
			throws NoSuchSIPException, IllegalOperationException,
			AcceptSIPException {

		long start = System.currentTimeMillis();

		AcceptSIPTask acceptSIPTask = null;
		try {

			acceptSIPTask = new AcceptSIPTask(fedoraClientUtility);

		} catch (RODAException e) {
			throw new AcceptSIPException("Action could not be performed - "
					+ e.getMessage(), e);
		}

		try {

			SIPState result = acceptSIPTask.acceptSIP(sipID, accept, reason,
					getClientUser().getName());

			long duration = System.currentTimeMillis() - start;

			registerAction("AcceptSIP.acceptSIP", new String[] { "sipID",
					sipID, "accept", new Boolean(accept).toString(), "reason",
					reason },
					"User %username% called method AcceptSIP.acceptSIP(sipID="
							+ sipID + ", accept="
							+ new Boolean(accept).toString() + ", reason="
							+ reason + ")", duration);

			return result;

		} catch (RODAException e) {
			throw new AcceptSIPException("Action could not be performed - "
					+ e.getMessage(), e);
		}

	}

}
