/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.submit.client;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface IngestSubmitServiceAsync {

	/**
	 * Submit SIPs as bytestreams
	 * 
	 * @param fileCodes
	 *            the codes of the uploaded bytestream
	 * 
	 * @return true if all SIPs were successfully submitted, false otherwise
	 * @throws LoginException
	 * @throws GenericException
	 * @throws RODAClientException
	 * @throws AuthorizationDeniedException 
	 */
	public void submitSIPs(String[] fileCodes, AsyncCallback<Boolean> callback);

	/**
	 * Create a new SIP
	 * 
	 * @param contentModel
	 *            the SIP content model
	 * 
	 * @param metadata
	 *            the SIP descriptive metadata
	 * @param fileCodes
	 *            the represention files' codes
	 * @param parentPID
	 *            the pid of the element which will be parent of the element
	 *            created with this SIP
	 * @return true if SIP successfully submited, false otherwise
	 * @throws LoginException
	 * @throws GenericException
	 */
	public void createSIP(String contentModel, DescriptionObject metadata,
			String[] fileCodes, String parentPID, AsyncCallback<Boolean> callback);

}
