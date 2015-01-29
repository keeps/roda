package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.resource.NotSupportedException;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.EditorHelper;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.EditorException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

/**
 * This class implements the Editor service.
 * 
 * @author Rui Castro
 */
public class Editor extends RODAWebService {

	static final private Logger logger = Logger.getLogger(Editor.class);

	/**
	 * Constructs a new instance of the {@link Editor} service.
	 * 
	 * @throws RODAServiceException
	 */
	public Editor() throws RODAServiceException {
		logger.info(getClass().getSimpleName() + " initialized OK"); //$NON-NLS-1$
	}

	/**
	 * Creates a new {@link DescriptionObject} that is child of
	 * {@link DescriptionObject} with pid <code>parentPID</code>. If
	 * <code>parentPID</code> is <code>null</code> the {@link DescriptionObject}
	 * must be a fonds.
	 * 
	 * @param dObject
	 *            the description object for the new description object.
	 * 
	 * @return the PID of the newly created {@link DescriptionObject}.
	 * 
	 * @throws InvalidDescriptionObjectException
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public String createDescriptionObject(DescriptionObject dObject)
			throws EditorException, NoSuchRODAObjectException,
			InvalidDescriptionObjectException {

		try {

			Date start = new Date();

			String result = getEditorHelper().createDescriptionObject(dObject);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.createDescriptionObject", new String[] {
					"descriptionObject", "" + dObject },
					"User %username% called method Editor.createDescriptionObject("
							+ dObject + ")", duration);

			return result;

		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}

	}

	/**
	 * Modifies {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the modified {@link DescriptionObject}.
	 * 
	 * @return the modified {@link DescriptionObject}.
	 * 
	 * @throws EditorException
	 * @throws NoSuchRODAObjectException
	 */
	public DescriptionObject modifyDescriptionObject(DescriptionObject dObject)
			throws EditorException, NoSuchRODAObjectException {

		try {

			Date start = new Date();

			DescriptionObject result = getEditorHelper()
					.modifyDescriptionObject(dObject);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.modifyDescriptionObject", new String[] {
					"descriptionObject", "" + dObject },
					"User %username% called method Editor.modifyDescriptionObject("
							+ dObject + ")", duration);

			return result;

		} catch (BrowserException e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}

	}

	/**
	 * Removes the {@link DescriptionObject} with PID <code>pid</code> and all
	 * it's descendants.
	 * 
	 * @param pid
	 *            the PID of the {@link DescriptionObject} to remove.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void removeDescriptionObject(String pid)
			throws NoSuchRODAObjectException, EditorException {

		try {

			Date start = new Date();

			getEditorHelper().removeDescriptionObject(pid);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.removeDescriptionObject", new String[] {
					"pid", pid },
					"User %username% called method Editor.removeDescriptionObject("
							+ pid + ")", duration);

		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}
	}

	/**
	 * Returns a list of possible {@link DescriptionLevel}s for the given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return an array of {@link DescriptionLevel}s.
	 * 
	 * @throws EditorException
	 */
	public DescriptionLevel[] getDOPossibleLevels(String doPID)
			throws EditorException {

		try {

			Date start = new Date();

			List<DescriptionLevel> possibleLevels = getEditorHelper()
					.getDOPossibleLevels(doPID);
			DescriptionLevel[] result = possibleLevels
					.toArray(new DescriptionLevel[possibleLevels.size()]);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.getDOPossibleLevels", new String[] {
					"doPID", doPID },
					"User %username% called method Editor.getPossibleLevels("
							+ doPID + ")", duration);

			return result;

		} catch (Throwable t) {
			logger.debug("Error getting possible levels for DO " + doPID
					+ " - " + t.getMessage(), t);
			throw new EditorException("Error getting possible levels for DO "
					+ doPID + " - " + t.getMessage(), t);
		}
	}

	/**
	 * Sets the permissions of the specified in the
	 * {@link RODAObjectPermissions} given.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions} to set.
	 * @param applyToDescendants
	 *            should this permissions be applied to all descendants?
	 * 
	 * @return the new permissions for the target {@link RODAObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public RODAObjectPermissions setRODAObjectPermissions(
			RODAObjectPermissions permissions, boolean applyToDescendants)
			throws NoSuchRODAObjectException, EditorException {

		try {

			Date start = new Date();

			RODAObjectPermissions result = getEditorHelper()
					.setRODAObjectPermissions(permissions, applyToDescendants,
							false);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.setRODAObjectPermissions", new String[] {
					"permitions", "" + permissions, "applyToDescendants",
					"" + applyToDescendants },
					"User %username% called method Editor.setRODAObjectPermissions("
							+ permissions + ", " + applyToDescendants + ")",
					duration);

			return result;

		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}
	}

	/**
	 * Sets the {@link Producers} for a Fonds.
	 * 
	 * @param doPID
	 *            the PID of the Fonds {@link DescriptionObject} or any of it's
	 *            descendants.
	 * 
	 * @param producers
	 *            the {@link Producers} to set.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void setProducers(String doPID, Producers producers)
			throws NoSuchRODAObjectException, EditorException {

		try {

			Date start = new Date();

			getEditorHelper().setDOProducers(doPID, producers);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Editor.setProducers", new String[] { "doPID",
					"" + doPID, "producers", "" + producers },
					"User %username% called method Editor.setProducers("
							+ doPID + ", " + producers + ")", duration);

		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}
	}

	
	public void modifyOtherMetadata(String doPID, String fileID, String otherMetadata) throws NoSuchRODAObjectException, NotSupportedException, EditorException, BrowserException, InvalidDescriptionObjectException, RemoteException, EadCMetadataException {
		logger.info("modifyOtherMetadata("+doPID+","+fileID+",...)");
		DescriptionObject originalDO = getEditorHelper().getBrowserHelper().getDescriptionObject(doPID);	//get the original DO
		
		// INFO implement here modification logic, if any
		logger.error("No modify other metadata logic is defined!");
	}
	

	private EditorHelper getEditorHelper() throws EditorException {

		CASUserPrincipal clientUser = getClientUser();
		FedoraClientUtility fedoraClientUtility = null;
		EditorHelper editorHelper = null;
		if (clientUser != null) {

				try {

					String fedoraURL = getConfiguration()
							.getString("fedoraURL");
					String fedoraGSearchURL = getConfiguration().getString(
							"fedoraGSearchURL");

				fedoraClientUtility = new FedoraClientUtility(fedoraURL,fedoraGSearchURL, clientUser, getCasUtility());
				editorHelper = new EditorHelper(fedoraClientUtility,getConfiguration());
				} catch (FedoraClientException e) {
					logger.debug("Error creating Fedora client - "
							+ e.getMessage(), e);
					throw new EditorException("Error creating Fedora client - "
							+ e.getMessage(), e);
				} catch (MalformedURLException e) {
					logger.debug("Bad URL for Fedora client - "
							+ e.getMessage(), e);
					throw new EditorException("Bad URL for Fedora client - "
							+ e.getMessage(), e);
				}

			return editorHelper;

		} else {

			throw new EditorException("User credentials are not available.");

		}
	}

}
