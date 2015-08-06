package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.AuthenticationException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class Downloader {
	static final private Logger logger = Logger.getLogger(Downloader.class);

	private URL serviceHost = null;

	private RODAClient rodaClient = null;
	private Browser browserService = null;

	private HttpClient httpClient = null;
	private GetMethod getFileMethod = null;

	/**
	 * Constructs a new authenticated {@link Downloader} for RODA Access
	 * service.
	 * 
	 * @param servicesHost
	 *            the {@link URL} to the host that hosts the RODA services.
	 * @param cup
	 *            the user
	 * @param casUtility
	 *            the CASUtility
	 * 
	 * @throws LoginException
	 * @throws DownloaderException
	 * @throws AuthenticationException 
	 */
	public Downloader(URL servicesHost, CASUserPrincipal cup, CASUtility casUtility)
			throws LoginException, DownloaderException {
		this.serviceHost = servicesHost;
		try {

			this.rodaClient = new RODAClient(servicesHost, cup,casUtility);
			this.browserService = this.rodaClient.getBrowserService();

		} catch (RODAClientException e) {
			logger.debug("Error creating RODA client - " + e.getMessage(), e);
			throw new DownloaderException("Error creating RODA client - "
					+ e.getMessage(), e);
		}

		// Set up the client connection
		this.httpClient = new HttpClient();
		this.httpClient.getParams().setAuthenticationPreemptive(true);
		
	}
	
	
	/**
	 * Constructs a new authenticated {@link Downloader} for RODA Access
	 * service.
	 * 
	 * @param servicesHost
	 *            the {@link URL} to the host that hosts the RODA services.
	 * @param username
	 *            the username to use in the connection to the service
	 * @param password
	 *            the password to use in the connection to the service
     * @param casUtility
	 *            the CASUtility
	 * 
	 * @throws LoginException
	 * @throws DownloaderException
	 * @throws AuthenticationException 
	 */
	public Downloader(URL servicesHost, String username, String password,CASUtility casUtility) throws LoginException, DownloaderException, AuthenticationException {
		// FIXME empty string
//			this(servicesHost,casUtility.getCASUserPrincipal(username, password),casUtility);
	}

	/**
	 * Gets the {@link InputStream} for a given {@link RepresentationFile}
	 * inside a {@link RepresentationObject}.
	 * 
	 * @param representationPID
	 *            the PID of the {@link RepresentationObject}.
	 * @param fileID
	 *            the ID of the {@link RepresentationFile}.
	 * 
	 * @return a {@link InputStream} for the file.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws DownloaderException
	 */
	public InputStream getFile(String PID, String fileID)
			throws NoSuchRODAObjectException, DownloaderException {

		if (PID == null || PID.trim().length() == 0) {
			throw new DownloaderException("PID cannot be null or empty");
		}
		if (fileID == null || fileID.trim().length() == 0) {
			throw new DownloaderException("fileID cannot be null or empty");
		}

		String accessURL = String.format("/get/%s/%s", PID, fileID);

		return get(accessURL);
	}

	/**
	 * Gets the {@link InputStream} for a given {@link RepresentationFile}
	 * inside a {@link RepresentationObject}.
	 * 
	 * @param accessURL
	 *            the access URL for {@link RODAObject} file.
	 * 
	 * @return a {@link InputStream} for the file.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws DownloaderException
	 */
	public InputStream get(String accessURL) throws DownloaderException {

		try {

			if (accessURL == null || accessURL.trim().length() == 0) {
				throw new DownloaderException(
						"accessURL cannot be null or empty");
			}

			String fileURL = String
					.format("%s/%s", this.serviceHost, accessURL);

			// Prepare method
			getFileMethod = getGetFileMethod(fileURL);

			logger.trace("HTTP GET " + fileURL);

			// Execute method
			this.httpClient.getParams().setAuthenticationPreemptive(true);
			String username = this.rodaClient.getUsername();
			String password = this.rodaClient.getCasUtility().generateProxyTicket(this.rodaClient.getProxyGrantingTicket());
			Credentials credentials = new UsernamePasswordCredentials(
					username, password);
			
			this.httpClient.getState().setCredentials(
					new AuthScope(this.serviceHost.getHost(), this.serviceHost
							.getPort(), AuthScope.ANY_REALM), credentials);
			int statusCode = this.httpClient.executeMethod(getFileMethod);

			
			if (statusCode == HttpStatus.SC_NOT_FOUND){
				throw new FileNotFoundException(accessURL+ " doesn't exist");
			}else if (statusCode != HttpStatus.SC_OK) {
				logger.error("HTTP GET failed - "
						+ getFileMethod.getStatusLine());
			}

			return getFileMethod.getResponseBodyAsStream();

		} catch (HttpException e) {
			logger.debug("Error downloading file - " + e.getMessage(), e);
			throw new DownloaderException("Error downloading file - "
					+ e.getMessage(), e);
		} catch (IOException e) {
			logger.debug("Error downloading file - " + e.getMessage(), e);
			throw new DownloaderException("Error downloading file - "
					+ e.getMessage(), e);
		} finally {

			// if (getFileMethod != null) {
			// getFileMethod.releaseConnection();
			// }
		}

	}

	/**
	 * Gets the {@link InputStream} for a given {@link RepresentationFile}
	 * inside a {@link RepresentationObject}.
	 * 
	 * @param representationPID
	 *            the PID of the {@link RepresentationObject}.
	 * @param fileID
	 *            the ID of the {@link RepresentationFile}.
	 * 
	 * @return a {@link InputStream} for the file.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws DownloaderException
	 */
	public InputStream get(String representationPID, String fileID)
			throws NoSuchRODAObjectException, DownloaderException {

		try {

			RepresentationObject rObject = this.browserService
					.getRepresentationObject(representationPID);

			String accessURL = null;

			if (rObject.getRootFile().getId().equals(fileID)) {
				accessURL = rObject.getRootFile().getAccessURL();
			} else {

				for (int i = 0; accessURL == null
						&& i < rObject.getPartFiles().length; i++) {

					if (rObject.getPartFiles()[i].getId().equals(fileID)) {
						accessURL = rObject.getPartFiles()[i].getAccessURL();
					}

				}
			}

			if (accessURL == null) {

				throw new DownloaderException(fileID
						+ " is not present in representation "
						+ representationPID);

			} else {

				return get(accessURL);
			}

		} catch (BrowserException e) {
			logger.debug("Remote service error - " + e.getMessage(), e);
			throw new DownloaderException("Remote service error - "
					+ e.getMessage(), e);
		} catch (RemoteException e) {
			logger.debug("Remote service error - " + e.getMessage(), e);
			throw new DownloaderException("Remote service error - "
					+ e.getMessage(), e);
		} finally {

			// if (getFileMethod != null) {
			// getFileMethod.releaseConnection();
			// }
		}

	}

	/**
	 * Saves the contents of a {@link RepresentationFile} in the specified
	 * directory.
	 * 
	 * @param representationPID
	 *            the PID of the {@link RepresentationObject}.
	 * @param fileID
	 *            the ID of the {@link RepresentationFile}.
	 * @param directory
	 *            the directory where to save the file.
	 * 
	 * @return the {@link File} created.
	 * 
	 * @throws DownloaderException
	 */
	public File saveTo(String representationPID, String fileID, File directory)
			throws DownloaderException {

		try {

			File outputFile = new File(directory, fileID);

			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

			@SuppressWarnings("unused")
			long copiedBytesCount = IOUtils.copyLarge(get(representationPID,
					fileID), fileOutputStream);

			fileOutputStream.close();

			return outputFile;

		} catch (NoSuchRODAObjectException e) {
			logger.debug(e.getMessage(), e);
			throw new DownloaderException(e.getMessage(), e);
		} catch (DownloaderException e) {
			logger.debug(e.getMessage(), e);
			throw new DownloaderException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage(), e);
			throw new DownloaderException(e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			throw new DownloaderException(e.getMessage(), e);
		}

	}

	private GetMethod getGetFileMethod(String fileURL) {
		if (this.getFileMethod != null) {
			this.getFileMethod.releaseConnection();
		}
		this.getFileMethod = new GetMethod(fileURL);

		return this.getFileMethod;
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.getFileMethod != null) {
			this.getFileMethod.releaseConnection();
		}
		super.finalize();
	}
}
