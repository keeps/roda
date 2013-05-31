package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.migrator.common.data.ConversionStatus;
import pt.gov.dgarq.roda.migrator.common.data.ConversionTicket;
import pt.gov.dgarq.roda.migrator.common.data.ConverstionRequest;

/**
 * This is the RODA Migrator service.
 * 
 * @author Rui Castro
 */
public class AsynchronousConverter {

	/**
	 * Get the agent used in this conversion/migration
	 * 
	 * @return the agent
	 */
	public AgentPreservationObject getAgent() {
		return null;
	}

	/**
	 * Request a conversion/migration
	 * 
	 * @param request
	 * @return a ticket which can be used to check the conversion status and
	 *         result
	 */
	public ConversionTicket requestConversion(ConverstionRequest request) {
		return null;
	}

	/**
	 * Check the conversion status
	 * 
	 * @param ticket
	 *            the conversion request ticket
	 * @return the conversion status or result
	 */
	public ConversionStatus checkConversionStatus(ConversionTicket ticket) {
		return null;
	}

}
