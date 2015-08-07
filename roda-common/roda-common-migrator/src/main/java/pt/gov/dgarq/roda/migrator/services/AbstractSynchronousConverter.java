package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * Converts images files using the Image Magick package
 * 
 * @author Rui Castro
 */
public abstract class AbstractSynchronousConverter extends ConverterWebService
		implements SynchronousConverter {

	private static final Logger logger = Logger
			.getLogger(AbstractSynchronousConverter.class);

	private RODAClient rodaClient = null;
	private Downloader rodaDownloader = null;

	/**
	 * @throws RODAServiceException
	 */
	public AbstractSynchronousConverter() throws RODAServiceException {
		super();
			String rodaServicesURL = getConfiguration().getString(
					"rodaServicesURL");
			String username = getClientUser().getName();
			String password = getClientUserPassword();

			String casURL = getConfiguration().getString("roda.cas.url");
			String coreURL = getConfiguration().getString("roda.core.url");
					
			CASUserPrincipal cup = null;
			CASUtility casUtility = null;
			
			
			try {
			casUtility = new CASUtility(new URL(casURL), new URL(coreURL));
			}catch(Throwable e){
				throw new RODAServiceException("Unable to create CASUtility - "+ e.getMessage(), e);
			}
				
			try{
				// FIXME empty string
				cup = casUtility.getCASUserPrincipal(username,password,"");
			}catch(Exception e){
				throw new RODAServiceException("Unable to get CASUserPrincipal - "+ e.getMessage(), e);
			}
			
			
			try {
				this.rodaDownloader = new Downloader(new URL(rodaServicesURL),
							cup,casUtility);
				
			} catch (DownloaderException e) {
				throw new RODAServiceException("Unable to create Downloader - "+ e.getMessage(), e);
			} catch (MalformedURLException e) {
				throw new RODAServiceException("Unable to create Downloader - "+ e.getMessage(), e);
			}
			try {
				rodaClient = new RODAClient(new URL(rodaServicesURL), cup,casUtility);
			} catch (RODAClientException e) {
				throw new RODAServiceException("Unable to create RodaClient - "+ e.getMessage(), e);
			} catch (MalformedURLException e) {
				throw new RODAServiceException("Unable to create RodaClient - "+ e.getMessage(), e);
			}

	}

	/**
	 * @see SynchronousConverter#getAgent()
	 */
	public AgentPreservationObject getAgent() throws ConverterException {

		AgentPreservationObject agent = new AgentPreservationObject();

		agent
				.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_MIGRATOR);
		agent.setAgentName(getVersion());

		return agent;
	}

	protected abstract String getVersion() throws ConverterException;

	/**
	 * Get representation files and writes them in the local disk
	 * 
	 * @param representation
	 * @return
	 * @throws IOException
	 * @throws DownloaderException
	 */
	protected LocalRepresentationObject downloadRepresentationToLocalDisk(
			RepresentationObject representation) throws IOException,
			DownloaderException {

		File tempDirectory = TempDir
				.createUniqueTemporaryDirectory("rodaSourceRep");

		logger.debug("Saving representation to " + tempDirectory);

		LocalRepresentationObject localRepresentation = new LocalRepresentationObject(
				tempDirectory, representation);

		RepresentationFile rootRepFile = representation.getRootFile();
		File rootFile = this.rodaDownloader.saveTo(representation.getPid(),
				rootRepFile.getId(), tempDirectory);
		localRepresentation.getRootFile().setAccessURL(
				rootFile.toURI().toURL().toString());

		logger.trace("File " + rootRepFile.getId() + " saved to " + rootFile);

		for (RepresentationFile partRepFile : localRepresentation
				.getPartFiles()) {

			File partFile = this.rodaDownloader.saveTo(localRepresentation
					.getPid(), partRepFile.getId(), tempDirectory);

			partRepFile.setAccessURL(partFile.toURI().toURL().toString());

			logger.trace("File " + partRepFile.getId() + " saved to "
					+ partFile);
		}

		return localRepresentation;
	}

	protected RepresentationObject moveToFinalDirectory(
			LocalRepresentationObject representation, File directory)
			throws IOException {

		File localDir = representation.getDirectory();
		
		FileUtils.moveDirectory(localDir, directory);

		representation.getRootFile().setAccessURL(
				getCacheURL(directory, representation.getRootFile().getId()));

		if (representation.getPartFiles() != null) {
			for (RepresentationFile partFile : representation.getPartFiles()) {
				partFile.setAccessURL(getCacheURL(directory, partFile.getId()));
			}
		}
		
		// Free local directory
		representation.setDirectory(null);
		FileUtils.deleteDirectory(localDir);

		return representation;
	}

	protected RODAClient getRodaClient() {
		return rodaClient;
	}

	protected Downloader getRodaDownloader() {
		return rodaDownloader;
	}

}
