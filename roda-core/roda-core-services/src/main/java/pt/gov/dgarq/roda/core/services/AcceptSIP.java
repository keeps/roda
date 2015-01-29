package pt.gov.dgarq.roda.core.services;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.gov.dgarq.roda.core.common.AcceptSIPException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.ingest.AcceptSIPTask;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

/**
 * @author Rui Castro
 * 
 */
public class AcceptSIP extends RODAWebService {
	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(AcceptSIP.class);

	private FedoraClientUtility fedoraClientUtility = null;
	
	private String recordOfReceiptPostURL = null;
	private String recordOfReceiptUsername = null;
	private String recordOfReceiptPassword = null;
	

	/**
	 * @throws RODAServiceException
	 */
	public AcceptSIP() throws RODAServiceException {
		super();
		init();
		
	}

	private void init() throws RODAServiceException {
		String fedoraURL = getConfiguration().getString("fedoraURL");
		String fedoraGSearchURL = getConfiguration().getString(
				"fedoraGSearchURL");
		recordOfReceiptPostURL = getConfiguration().getString("recordOfReceiptPostURL");
		recordOfReceiptUsername = getConfiguration().getString("recordOfReceiptUsername");
		recordOfReceiptPassword = getConfiguration().getString("recordOfReceiptPassword");
		

		CASUserPrincipal clientUser = getClientUser();

		if (clientUser != null) {

			try {
				this.fedoraClientUtility = new FedoraClientUtility(fedoraURL,fedoraGSearchURL, clientUser, getCasUtility());

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
			if(fedoraClientUtility==null){
				init();
			}
			
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
			
			start = System.currentTimeMillis();
			
			boolean sent = sendRecordOfReceipt(sipID,accept,reason,result.getIngestedPID(),result.getParentPID(),result.getDatetime());
			
			duration = System.currentTimeMillis() - start;
			
			
			if(sent){
				registerAction("AcceptSIP.sendRecordOfReceipt", new String[] { "sipID",
						sipID,"recordOfReceiptPostURL",recordOfReceiptPostURL,"recordOfReceiptUsername",recordOfReceiptUsername,"recordOfReceiptPassword",recordOfReceiptPassword},
						"User %username% called method AcceptSIP.sendRecordOfReceipt(sipID="
								+ sipID + ", recordOfReceiptPostURL="+recordOfReceiptPostURL+")", duration);
			}
			
			

			

			return result;

		} catch (RODAException e) {
			throw new AcceptSIPException("Action could not be performed - "
					+ e.getMessage(), e);
		}

	}

	private boolean sendRecordOfReceipt(String sipID, boolean accept,String reason, String ingestedPID,String parentPID, Date datetime) throws AcceptSIPException {
		boolean sent = false;
		if(recordOfReceiptPostURL!=null && !recordOfReceiptPostURL.trim().equalsIgnoreCase("")){
			try {
				URL postURL = new URL(recordOfReceiptPostURL);
				PostMethod postMethod = new PostMethod(recordOfReceiptPostURL);
				String postData = generatePostData(sipID, accept, reason, ingestedPID, parentPID, datetime);
				StringRequestEntity r = new StringRequestEntity(postData,"text/plain","UTF-8");
				
				postMethod.setRequestEntity(r);
				HttpClient client = new HttpClient();
				
				if(recordOfReceiptUsername!=null && !recordOfReceiptUsername.trim().equalsIgnoreCase("")){
					Credentials credentials = new UsernamePasswordCredentials(recordOfReceiptUsername,recordOfReceiptPassword);
					client.getState().setCredentials(new AuthScope(postURL.getHost(),postURL.getPort()), credentials);
				}
				int status = client.executeMethod(postMethod);
				
				if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
					sent = true;
				}
			} catch (HttpException e) {
				logger.error("Error sending record of receipt: "+e.getMessage(), e);
				throw new AcceptSIPException("Error sending record of receipt: "+e.getMessage(),e);
			} catch (IOException e) {
				logger.error("Error sending record of receipt: "+e.getMessage(), e);
				throw new AcceptSIPException("Error sending record of receipt: "+e.getMessage(),e);
			} catch (ParserConfigurationException e) {
				logger.error("Error sending record of receipt: "+e.getMessage(), e);
				throw new AcceptSIPException("Error sending record of receipt: "+e.getMessage(),e);
			} catch (TransformerException e) {
				logger.error("Error sending record of receipt: "+e.getMessage(), e);
				throw new AcceptSIPException("Error sending record of receipt: "+e.getMessage(),e);
			}
		}
		return sent;
		
	}

	private String generatePostData(String sipID, boolean accept, String reason, String ingestedPID, String parentPID, Date datetime) throws ParserConfigurationException, TransformerException {
		 DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
         Document doc = docBuilder.newDocument();

         Element root = doc.createElement("receipt");
         doc.appendChild(root);
         
         Element sipIDElement = doc.createElement("sipID");
         root.appendChild(sipIDElement);
         sipIDElement.appendChild(doc.createTextNode(sipID));
         
         Element acceptElement = doc.createElement("accept");
         root.appendChild(acceptElement);
         acceptElement.appendChild(doc.createTextNode(""+accept));
         
         Element reasonElement = doc.createElement("reason");
         root.appendChild(reasonElement);
         reasonElement.appendChild(doc.createTextNode(reason));
         
         Element ingestedPIDElement = doc.createElement("ingestedPID");
         root.appendChild(ingestedPIDElement);
         ingestedPIDElement.appendChild(doc.createTextNode(ingestedPID));
         
         Element parentPIDElement = doc.createElement("parentPID");
         root.appendChild(parentPIDElement);
         parentPIDElement.appendChild(doc.createTextNode(parentPID));
         
         Element datetimeElement = doc.createElement("datetime");
         root.appendChild(datetimeElement);
         datetimeElement.appendChild(doc.createTextNode(datetime.toString()));

         TransformerFactory transfac = TransformerFactory.newInstance();
         Transformer trans = transfac.newTransformer();
         trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         trans.setOutputProperty(OutputKeys.INDENT, "yes");

         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult(sw);
         DOMSource source = new DOMSource(doc);
         trans.transform(source, result);
         return sw.toString();
	}

}
