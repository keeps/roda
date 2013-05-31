package pt.gov.dgarq.roda.core.ingest;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.SIPState;

/**
 * This task drops an uploaded {@link SIPState} into the ingest pipeline.
 * 
 * @author Rui Castro
 */
public class SIPUploadTask extends IngestTask {
	static final private Logger logger = Logger.getLogger(SIPUploadTask.class);

	private FileItem fileItem = null;

	/**
	 * @param fileItem
	 * 
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public SIPUploadTask(FileItem fileItem) throws InvalidIngestStateException,
			IngestRegistryException {
		super();

		this.fileItem = fileItem;
	}

	/**
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		StringBuffer report = new StringBuffer();

		try {

			File finalStateFile = getFinalStateLocation(sipState);
			File parentDirectory = finalStateFile.getParentFile();

			if (parentDirectory.exists()) {
				if (finalStateFile.exists()) {
					FileUtils.forceDelete(finalStateFile);
				}
			} else {
				parentDirectory.mkdirs();
			}

			fileItem.write(finalStateFile);

			logger.debug("SIP upload OK and droped in " + finalStateFile); //$NON-NLS-1$

			report.append(String.format(Messages
					.getString("SIPUploadTask.UPLOAD_SUCCESSFULL"), sipState //$NON-NLS-1$
					.getOriginalFilename()));

			return new IngestTaskResult(true, report.toString());

		} catch (Exception e) {
			throw new IngestTaskException("Error saving SIP to disk - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}

	}

	/**
	 * 
	 * @see IngestTask#undoTask(SIPState, IngestTaskResult)
	 */
	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {
		// Remove SIP contents already unpacked to new final state directory.
		try {

			deleteSIPFilesFromState(sip, getFinalState());

		} catch (IngestTaskException e) {
			logger.warn("Exception performing undo on SIP " + sip.getId() //$NON-NLS-1$
					+ ". Ignoring and leaving the garbage behind.", e); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 * 
	 * @see IngestTask#doCleanup(SIPState, IngestTaskResult)
	 */
	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Nothing to clean up
	}

	/**
	 * Insert a SIP into the ingest pipeline.
	 * 
	 * @param username
	 *            the name of the user that uploaded the SIP.
	 * @param originalFilename
	 *            the SIP filename.
	 * 
	 * @return the inserted {@link SIPState}.
	 * 
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 * @throws IngestTaskException
	 */
	public SIPState insertSIP(String username, String originalFilename)
			throws InvalidIngestStateException, IngestRegistryException,
			IngestTaskException {

		return super.registerSIP(username, originalFilename, getFinalState());

	}

}
