/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.Map;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import org.roda.legacy.exception.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

/**
 * @author Luis Faria
 * 
 */
public interface IngestListServiceAsync {

	/**
	 * Get a SIPState with its SIP id
	 * 
	 * @param sipId
	 * @return the SIP State or null if the sipId does not exist
	 * @throws RODAException
	 */
	public void getSipState(String sipId, AsyncCallback<SIPState> callback);

	/**
	 * Get SIP count
	 * 
	 * @param filter
	 * @return
	 * @throws RODAException
	 */
	public void getSIPCount(Filter filter, AsyncCallback<Integer> callback);

	/**
	 * Get SIP list
	 * 
	 * @param adapter
	 * @return
	 * @throws RODAException
	 */
	public void getSIPs(ContentAdapter adapter, AsyncCallback<SIPState[]> callback);

	/**
	 * Accept a SIP
	 * 
	 * @param sipId
	 * @param message
	 * @throws RODAException
	 */
	public void acceptSIP(String sipId, String message, AsyncCallback<Void> callback);

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
	public void rejectSIP(String sipId, String message, boolean notifyProducer, AsyncCallback<Void> callback);

	/**
	 * Set SIP list report info parameters
	 * 
	 * @param adapter
	 * @param locale
	 * @throws RODAException
	 * @throws PrintReportException
	 */
	public void setSIPListReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback);

	/**
	 * Get accept message templates
	 * 
	 * @param localeString
	 * @return
	 */
	public void getAcceptMessageTemplates(String localeString, AsyncCallback<Map<String, String>> callback);

	/**
	 * Get reject message templates
	 * 
	 * @param localeString
	 * @return
	 */
	public void getRejectMessageTemplates(String localeString, AsyncCallback<Map<String, String>> callback);

}
