/**
 * 
 */
package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;

/**
 * @author Luis Faria
 * 
 */
public class SipSendUtility {
	private static Logger logger = Logger.getLogger(SipSendUtility.class);

	public static PartSource createPartSource(final InputStream stream,
			final String fileName, final long length) {
		return new PartSource() {

			public InputStream createInputStream() throws IOException {
				return stream;
			}

			public String getFileName() {
				return fileName;
			}

			public long getLength() {
				return length;
			}

		};
	}

	public static boolean sendSIPs(PartSource[] sipSources,
			RODAClient rodaClient) throws IOException {

		boolean success = false;

		PostMethod filePost = new PostMethod(rodaClient.getIngestSubmitUrl().toString());

		Part[] parts = new Part[sipSources.length];
		for (int i = 0; i < sipSources.length; i++) {
			parts[i] = new FilePart(sipSources[i].getFileName(), sipSources[i]);
		}

		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
				.getParams()));

		HttpClient client = new HttpClient();
		client.getParams().setAuthenticationPreemptive(true);
		
		
		String username = rodaClient.getUsername();
		String password = rodaClient.getProxyGrantingTicket();

		Credentials credentials = new UsernamePasswordCredentials(username,password);
		client.getState().setCredentials(
				new AuthScope(rodaClient.getIngestSubmitUrl().getHost(), rodaClient.getIngestSubmitUrl().getPort(),
						AuthScope.ANY_REALM), credentials);
		int status = client.executeMethod(filePost);

		if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
			success = true;
			logger.debug("SIP Upload Successful.");

		} else {
			success = false;
			logger.error("Upload error - HTTP Status: " + status + " - "
					+ HttpStatus.getStatusText(status));
		}

		return success;
	}

	/**
	 * Send a SIP file to the ingest service
	 * 
	 * @param sipFile
	 *            the packaged SIP file
	 * @param rodaClient
	 *            the RODA client to use in authentication
	 * @return true if SIP was sent successfully
	 * @throws IOException
	 */
	public static boolean sendSIP(File sipFile, RODAClient rodaClient)
			throws IOException {
		PartSource partSource = new FilePartSource(sipFile);
		return sendSIPs(new PartSource[] { partSource }, rodaClient);
	}

	/**
	 * Send a list of SIP files to the ingest service
	 * 
	 * @param sipFiles
	 *            the packaged SIP files
	 * @param rodaClient
	 *            the RODA client to use in authentication
	 * @return true if all SIPs were sent successfully, false if otherwise
	 * @throws IOException
	 */
	public static boolean sendSIP(List<File> sipFiles, RODAClient rodaClient)
			throws IOException {
		PartSource[] partSources = new PartSource[sipFiles.size()];
		int i = 0;
		for (File sipFile : sipFiles) {
			partSources[i++] = new FilePartSource(sipFile);
		}
		return sendSIPs(partSources, rodaClient);
	}
}
