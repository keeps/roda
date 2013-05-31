package pt.gov.dgarq.roda.wui.ingest.submit.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.fileupload.server.FileUpload;
import pt.gov.dgarq.roda.wui.ingest.submit.client.IngestSubmitService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Ingest submit service implementation
 * 
 * @author Luis Faria
 */
public class IngestSubmitServiceImpl extends RemoteServiceServlet implements
		IngestSubmitService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(IngestSubmitServiceImpl.class);

	protected static String getFileName(String fileItemName) {
		String ret;
		int indexOfSlash = fileItemName.lastIndexOf('\\');
		if (indexOfSlash == -1) {
			ret = fileItemName;
		} else {
			ret = fileItemName.substring(indexOfSlash + 1);
		}
		return ret;
	}

	public boolean submitSIPs(String[] fileCodes) throws LoginException,
			RODAClientException, GenericException, AuthorizationDeniedException {
		boolean allsubmitted;
		HttpSession session = getThreadLocalRequest().getSession();
		FileItem[] items = FileUpload.lookupFileItems(session, fileCodes);
		if (items != null) {
			PartSource[] sipPartSources = new PartSource[items.length];
			for (int i = 0; i < sipPartSources.length; i++) {
				final FileItem item = items[i];
				sipPartSources[i] = new PartSource() {

					public InputStream createInputStream() throws IOException {
						return item.getInputStream();
					}

					public String getFileName() {
						return IngestSubmitServiceImpl.getFileName(item
								.getName());
					}

					public long getLength() {
						return item.getSize();
					}

				};
			}
			allsubmitted = sendSIPs(sipPartSources);
		} else {
			allsubmitted = false;
		}
		return allsubmitted;

	}

	public boolean createSIP(String contentModel, DescriptionObject metadata,
			String[] fileCodes, String parentPID) throws LoginException,
			GenericException {
		HttpSession session = getThreadLocalRequest().getSession();
		FileItem[] items = null;
		StreamRepresentationObject rep;
		boolean success;
		logger.debug("Creating and sending SIP");

		SIP sip = null;
		File sipTempFile = null;
		try {
			logger.debug("Getting uploaded files");
			items = FileUpload.lookupFileItems(session, fileCodes);

			if (items != null) {
				List<String> filenames = new Vector<String>();
				List<InputStream> streams = new Vector<InputStream>();

				for (FileItem item : items) {
					filenames.add(IngestSubmitServiceImpl.getFileName(item
							.getName()));
					streams.add(item.getInputStream());
				}

				rep = RepresentationBuilder.createRepresentation(contentModel,
						filenames, streams);

				List<StreamRepresentationObject> reps = new Vector<StreamRepresentationObject>();
				reps.add(rep);
				logger.debug("Creating SIP");

				sip = SIPUtility.createSIP(parentPID, metadata, reps);

				sipTempFile = File.createTempFile("roda", ".sip", TempDir
						.getTemporaryDirectory());
				SIPUtility.writeSIPPackage(sip, sipTempFile);

				FileInputStream sipStream = new FileInputStream(sipTempFile);

				logger.debug("Sending SIP");

				String sipName = createSipName(parentPID, metadata);
				success = sendSIPs(new PartSource[] { createPartSource(
						sipStream, sipName, sipStream.getChannel().size()) });

				logger.debug("Done creating and sending SIP");
			} else {
				success = false;
			}
		} catch (IOException e) {
			logger.error("Error creating representation stream", e);
			throw new GenericException(e.getMessage());
		} catch (Exception e) {
			logger.error("Error creating representation stream", e);
			throw new GenericException(e.getMessage());
		} finally {
			if (sip != null && sip.getDirectory() != null) {
				FileUtils.deleteQuietly(sip.getDirectory());
			}

			if (sipTempFile != null) {
				sipTempFile.delete();
			}
		}

		return success;
	}

	private String createSipName(String parentPid, DescriptionObject metadata)
			throws LoginException, RODAClientException {
		RODAClient rodaClient = RodaClientFactory
				.getRodaClient(getThreadLocalRequest().getSession());
		String name;
		try {
			// Get parent complete reference
			DescriptionObject parentDO = rodaClient.getBrowserService()
					.getDescriptionObject(parentPid);
			String[] referenceSplit = parentDO.getCompleteReference()
					.split("/");

			// Remove country code and repository code from reference and
			// replace backslash with underscore
			name = "";
			for (int i = 2; i < referenceSplit.length; i++) {
				name += referenceSplit[i] + "_";
			}
			// add the sip id
			name += metadata.getId();

		} catch (Exception e) {
			name = metadata.getId();
		}

		// add the sip filename extension
		name += ".sip";

		return name;
	}

	protected PartSource createPartSource(final InputStream stream,
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

	private boolean sendSIPs(PartSource[] sipSources) throws LoginException,
			GenericException, RODAClientException, AuthorizationDeniedException {

		boolean success = false;

		try {
			URL sipUploadURL = RodaClientFactory.getIngestSubmitUrl();

			PostMethod filePost = new PostMethod(sipUploadURL.toString());

			Part[] parts = new Part[sipSources.length];
			for (int i = 0; i < sipSources.length; i++) {
				parts[i] = new FilePart(sipSources[i].getFileName(),
						sipSources[i]);
			}

			filePost.setRequestEntity(new MultipartRequestEntity(parts,
					filePost.getParams()));

			HttpClient client = new HttpClient();
			client.getParams().setAuthenticationPreemptive(true);
			RODAClient rodaClient = RodaClientFactory
					.getRodaClient(getThreadLocalRequest().getSession());

			Credentials credentials = new UsernamePasswordCredentials(
					rodaClient.getUsername(), rodaClient.getPassword());
			client.getState().setCredentials(
					new AuthScope(sipUploadURL.getHost(), sipUploadURL
							.getPort(), AuthScope.ANY_REALM), credentials);

			int status = client.executeMethod(filePost);

			logger.trace("Responde Status: " + status + " - "
					+ HttpStatus.getStatusText(status));

			if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
				success = true;
				logger.debug("SIP Upload Successful.");

			} else if (status == HttpStatus.SC_UNAUTHORIZED) {
				success = false;
				throw new AuthorizationDeniedException(
						"Not authorized to upload SIPs");
			} else {
				success = false;
				logger.error("Upload error - HTTP Status: " + status + " - "
						+ HttpStatus.getStatusText(status));
			}

		} catch (HttpException e) {
			logger.error("Error executing POST method", e);
			throw new GenericException(e.getMessage());
		} catch (IOException e) {
			logger.error("Error getting POST response", e);
			throw new GenericException(e.getMessage());
		}

		return success;
	}

}
