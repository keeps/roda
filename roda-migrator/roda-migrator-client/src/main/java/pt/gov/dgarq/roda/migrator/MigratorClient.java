package pt.gov.dgarq.roda.migrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.stubs.SynchronousConverter;
import pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterServiceLocator;
import pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterSoapBindingStub;

/**
 * @author Rui Castro
 */
public class MigratorClient {

	static final private Logger logger = Logger.getLogger(MigratorClient.class);

	/**
	 * Constructs a new anonymous MigratorClient for the specified RODA Services
	 * host.
	 */
	public MigratorClient() {
	}

	/**
	 * Download the files of the specified {@link RepresentationObject} to the
	 * specified directory.
	 * 
	 * @param representation
	 *            the {@link RepresentationObject}.
	 * @param directory
	 *            the directory where to save the representation files. If the
	 *            directory doesn't exist it will be created.
	 * 
	 * @throws MigratorClientException
	 * 
	 * @deprecated Use
	 *             {@link #writeRepresentationObject(RepresentationObject, File)}
	 *             instead.
	 */
	public void saveRepresentation(RepresentationObject representation,
			File directory) throws MigratorClientException {

		if (!directory.exists()) {
			if (!directory.mkdir()) {
				logger.debug("Couldn't create directory " + directory);
				throw new MigratorClientException("Couldn't create directory "
						+ directory);
			}
		}

		// Set up the client connection
		HttpClient httpClient = new HttpClient();

		writeRepresentationFile(httpClient, representation.getRootFile(),
				directory);

		if (representation.getPartFiles() != null) {
			for (RepresentationFile partFile : representation.getPartFiles()) {
				writeRepresentationFile(httpClient, partFile, directory);
			}
		}

	}

	/**
	 * Write the files of the specified {@link RepresentationObject} to the
	 * specified directory.
	 * 
	 * @param representation
	 *            the {@link RepresentationObject}.
	 * @param directory
	 *            the directory where to write the representation files. If the
	 *            directory doesn't exist it will be created.
	 * 
	 * @return a {@link RepresentationObject} with the local files.
	 * 
	 * @throws MigratorClientException
	 */
	public RepresentationObject writeRepresentationObject(
			RepresentationObject representation, File directory)
			throws MigratorClientException {

		if (!directory.exists()) {
			if (!directory.mkdir()) {
				logger.debug("Couldn't create directory " + directory);
				throw new MigratorClientException("Couldn't create directory "
						+ directory);
			}
		}

		// Set up the client connection
		HttpClient httpClient = new HttpClient();

		RepresentationFile rootFile = writeRepresentationFile(httpClient,
				representation.getRootFile(), directory);

		List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();

		if (representation.getPartFiles() != null) {
			for (RepresentationFile partFile : representation.getPartFiles()) {
				partFiles.add(writeRepresentationFile(httpClient, partFile,
						directory));
			}
		}

		RepresentationObject writtenRO = new RepresentationObject(
				representation);
		writtenRO.setRootFile(rootFile);
		writtenRO.setPartFiles(partFiles
				.toArray(new RepresentationFile[partFiles.size()]));
		return writtenRO;
	}

	/**
	 * @param representation
	 * @throws MigratorClientException
	 */
	public void deleteCachedRepresentation(RepresentationObject representation)
			throws MigratorClientException {

		String accessURL = representation.getRootFile().getAccessURL();
		String representationURL = accessURL.substring(0, accessURL
				.lastIndexOf("/"));

		// Set up the client connection
		HttpClient httpClient = new HttpClient();

		// Prepare method
		DeleteMethod deleteMethod = new DeleteMethod(representationURL);

		try {

			// Execute method
			int statusCode = httpClient.executeMethod(deleteMethod);

			if (statusCode != HttpStatus.SC_OK) {
				logger
						.error("Error deleting cached representation - HTTP DELETE failed - "
								+ deleteMethod.getStatusLine());
				throw new MigratorClientException(
						"Error deleting cached representation - HTTP DELETE failed - "
								+ deleteMethod.getStatusLine());
			}

		} catch (HttpException e) {
			logger.debug(e.getMessage(), e);
			throw new MigratorClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			throw new MigratorClientException(e.getMessage(), e);
		} finally {

			if (deleteMethod != null) {
				deleteMethod.releaseConnection();
			}
		}

	}

	/**
	 * Returns a stub to the {@link SynchronousConverter} service.
	 * 
	 * @param serviceURL
	 * @param username
	 * @param password
	 * 
	 * @return the {@link SynchronousConverter}
	 * 
	 * @throws MigratorClientException
	 */
	public SynchronousConverter getSynchronousConverterService(
			String serviceURL, String username, String password)
			throws MigratorClientException {

		SynchronousConverter converter;

		SynchronousConverterServiceLocator serviceLocator = new SynchronousConverterServiceLocator();
		serviceLocator.setSynchronousConverterEndpointAddress(serviceURL);
		try {

			converter = serviceLocator.getSynchronousConverter();

		} catch (ServiceException e) {
			logger.debug("Error accessing service - " + e.getMessage(), e);
			throw new MigratorClientException("Error accessing service - "
					+ e.getMessage(), e);
		}

		((SynchronousConverterSoapBindingStub) converter).setUsername(username);
		((SynchronousConverterSoapBindingStub) converter).setPassword(password);
		// Disable timeout. Conversions may take long time.
		((SynchronousConverterSoapBindingStub) converter).setTimeout(0);

		return converter;
	}

	private RepresentationFile writeRepresentationFile(HttpClient httpClient,
			RepresentationFile rFile, File directory)
			throws MigratorClientException {

		// Prepare method
		GetMethod getMethod = new GetMethod(rFile.getAccessURL());

		try {

			logger.trace("HTTP GET " + getMethod.getURI());

			int statusCode = httpClient.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {

				logger.debug("Error getting file - HTTP GET failed - "
						+ getMethod.getStatusLine());
				throw new MigratorClientException(
						"Error getting file - HTTP GET failed - "
								+ getMethod.getStatusLine());

			} else {

				File outputFile = new File(directory, rFile.getId());
				IOUtils.copyLarge(getMethod.getResponseBodyAsStream(),
						new FileOutputStream(outputFile));

				return new RepresentationFile(rFile.getId(), rFile
						.getOriginalName(), rFile.getMimetype(), outputFile
						.length(), outputFile.toURI().toURL().toExternalForm());
			}

		} catch (HttpException e) {
			logger.debug(e.getMessage(), e);
			throw new MigratorClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
			throw new MigratorClientException(e.getMessage(), e);
		} finally {
			// Always release the connection
			getMethod.releaseConnection();
		}

	}

}
