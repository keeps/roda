package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * @author Rui Castro
 */
public class Uploader {

	static final private Logger logger = Logger.getLogger(Uploader.class);

	private static final String fileUploadLocation = "/roda-core/FileUpload";

	private URL serviceHost = null;

	private String username = null;
	private String password = null;

	private URL fileUploadURL = null;

	/**
	 * Constructs a new authenticated {@link Uploader} for RODA file upload
	 * service.
	 * 
	 * @param servicesHost
	 *            the {@link URL} to the host that hosts the RODA services.
	 * @param username
	 *            the username to use in the connection to the service
	 * @param password
	 *            the password to use in the connection to the service
	 * 
	 * @throws MalformedURLException
	 */
	public Uploader(URL servicesHost, String username, String password)
			throws MalformedURLException {

		this.serviceHost = servicesHost;
		this.username = username;
		this.password = password;

		this.fileUploadURL = new URL(this.serviceHost, fileUploadLocation);
	}

	/**
	 * Upload a {@link RepresentationObject}'s {@link File}.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject} to which this file
	 *            belongs.
	 * @param fileID
	 *            the file ID.
	 * @param file
	 *            the {@link File} to upload.
	 * 
	 * @throws FileNotFoundException
	 *             if the specified {@link File} cannot be found.
	 * @throws UploadException
	 *             if something goes wrong with the upload.
	 */
	public void uploadRepresentationFile(String roPID, String fileID, File file)
			throws UploadException, FileNotFoundException {

		logger.debug("uploadURL: " + this.fileUploadURL.toString());

		PostMethod postMethod = new PostMethod(this.fileUploadURL.toString());
		// postMethod.setFollowRedirects(true);

		Part[] parts = { new StringPart("pid", roPID),
				new StringPart("id", fileID), new FilePart("file", file) };

		postMethod.setRequestEntity(new MultipartRequestEntity(parts,
				postMethod.getParams()));

		HttpClient client = new HttpClient();

		client.getState().setCredentials(
				new AuthScope(this.fileUploadURL.getHost(), this.fileUploadURL
						.getPort()),
				new UsernamePasswordCredentials(this.username, this.password));

		try {

			int status = client.executeMethod(postMethod);

			logger.debug("Response Status: " + status + " - "
					+ HttpStatus.getStatusText(status));

			if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
				logger.info("Upload Successful. OK");
			} else {
				logger.error("Upload error: "
						+ HttpStatus.getStatusText(status) + " FAILED");
				throw new UploadException("Error uploading file: "
						+ HttpStatus.getStatusText(status));
			}
			String uploadResponse = postMethod.getResponseBodyAsString().trim();

			logger.debug("Upload response: " + uploadResponse);

			// return uploadedFileURL;

		} catch (HttpException e) {
			throw new UploadException(e.getMessage(), e);
		} catch (IOException e) {
			throw new UploadException(e.getMessage(), e);
		}

	}

	/**
	 * Upload a {@link RepresentationObject}'s {@link RepresentationFile}.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject} to which this file
	 *            belongs.
	 * @param rFile
	 *            the {@link RepresentationFile} to upload.
	 * 
	 * @throws FileNotFoundException
	 *             if the specified {@link File} cannot be found.
	 * @throws UploadException
	 *             if something goes wrong with the upload.
	 */
	public void uploadRepresentationFile(String roPID, RepresentationFile rFile)
			throws UploadException, FileNotFoundException {

		logger.debug("uploadURL: " + this.fileUploadURL.toString());

		PostMethod postMethod = new PostMethod(this.fileUploadURL.toString());
		// postMethod.setFollowRedirects(true);

		logger
				.trace("Representation file accessURL is "
						+ rFile.getAccessURL());
		File fileToUpload = new File(URI.create(rFile.getAccessURL()));
		logger.trace("Representation file is " + fileToUpload);

		long fileSize = fileToUpload.length();

		if (rFile.getSize() != fileSize) {
			logger
					.warn("File size information in RepresentationFile differs from the real file size.");
			logger.warn("Using the real file size; " + fileSize + " bytes");
		}

		Part[] parts = { new StringPart("pid", roPID),
				new StringPart("id", rFile.getId()),
				new StringPart("name", rFile.getOriginalName(), "UTF-8"),
				new StringPart("mimetype", rFile.getMimetype()),
				new StringPart("size", Long.toString(fileToUpload.length())),
				new FilePart("file", fileToUpload) };

		logger.debug("Upload pid: " + roPID);
		logger.debug("Upload id: " + rFile.getId());
		logger.debug("Upload name: " + rFile.getOriginalName());
		logger.debug("Upload mimetype: " + rFile.getMimetype());
		logger.debug("Upload size: " + Long.toString(fileToUpload.length()));
		logger.debug("Upload file: " + fileToUpload);

		postMethod.setRequestEntity(new MultipartRequestEntity(parts,
				postMethod.getParams()));

		HttpClient client = new HttpClient();

		client.getState().setCredentials(
				new AuthScope(this.fileUploadURL.getHost(), this.fileUploadURL
						.getPort()),
				new UsernamePasswordCredentials(this.username, this.password));

		try {

			int status = client.executeMethod(postMethod);

			logger.debug("Response Status: " + status + " - "
					+ HttpStatus.getStatusText(status));

			if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
				logger.info("Upload Successful. OK");
			} else {
				logger.error("Upload error: "
						+ HttpStatus.getStatusText(status) + " FAILED");
				throw new UploadException("Error uploading file: "
						+ HttpStatus.getStatusText(status));
			}

			InputStream responseInputStream = postMethod
					.getResponseBodyAsStream();

			StringWriter stringWriter = new StringWriter();
			IOUtils.copy(responseInputStream, stringWriter);

			String uploadResponse = stringWriter.toString();

			stringWriter.close();

			logger.debug("Upload response: " + uploadResponse);

		} catch (HttpException e) {
			throw new UploadException(e.getMessage(), e);
		} catch (IOException e) {
			throw new UploadException(e.getMessage(), e);
		}

	}

}
