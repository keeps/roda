/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.Map;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

/**
 * @author Luis Faria
 * 
 */
public interface IngestListService extends RemoteService {

	/**
	 * Ingest list service URI
	 */
	public static final String SERVICE_URI = "ingestlist";

	/**
	 * Utilities
	 */
	public static class Util {

		/**
		 * Get service instance
		 * 
		 * @return
		 */
		public static IngestListServiceAsync getInstance() {

			IngestListServiceAsync instance = (IngestListServiceAsync) GWT
					.create(IngestListService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}

	/**
	 * Get a SIPState with its SIP id
	 * 
	 * @param sipId
	 * @return the SIP State or null if the sipId does not exist
	 * @throws RODAException
	 */
	public SIPState getSipState(String sipId) throws RODAException;

	/**
	 * Get SIP count
	 * 
	 * @param filter
	 * @return
	 * @throws RODAException
	 */
	public int getSIPCount(Filter filter) throws RODAException;

	/**
	 * Get SIP list
	 * 
	 * @param adapter
	 * @return
	 * @throws RODAException
	 */
	public SIPState[] getSIPs(ContentAdapter adapter) throws RODAException;

	/**
	 * Accept a SIP
	 * 
	 * @param sipId
	 * @param message 
	 * @throws RODAException
	 */
	public void acceptSIP(String sipId, String message) throws RODAException;

	/**
	 * Reject a SIP
	 * 
	 * @param sipId
	 * @param message
	 *            message detailing the cause of the rejection
	 * @param notifyProducer
	 *            where to notify the producer of the SIP by email about this
	 *            rejection
	 * @throws RODAException
	 */
	public void rejectSIP(String sipId, String message, boolean notifyProducer)
			throws RODAException;

	/**
	 * Set SIP list report info parameters
	 * 
	 * @param adapter
	 * @param locale
	 * @throws RODAException
	 * @throws PrintReportException
	 */
	public void setSIPListReportInfo(ContentAdapter adapter, String locale)
			throws PrintReportException;

	
	/**
	 * Get accept message templates
	 * 
	 * @param localeString
	 * @return
	 */
	public Map<String, String> getAcceptMessageTemplates(String localeString);
	
	/**
	 * Get reject message templates
	 * 
	 * @param localeString
	 * @return
	 */
	public Map<String, String> getRejectMessageTemplates(String localeString);

}
