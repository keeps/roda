package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.BrowserHelper;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRepresentationFileException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;

/**
 * This class implements the Browser service.
 * 
 * @author Rui Castro
 */
public class Browser extends RODAWebService {

	static final private Logger logger = Logger.getLogger(Browser.class);

	private Map<String, BrowserHelper> browserHelperCache = new HashMap<String, BrowserHelper>();

	/**
	 * Constructs a new instance of Browser service.
	 * 
	 * @throws RODAServiceException
	 */
	public Browser() throws RODAServiceException {

		super();

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Returns the {@link RODAObject} with the given PID.
	 * 
	 * @param pid
	 *            the PID of the object.
	 * 
	 * @return a {@link RODAObject} for the given PID.
	 * 
	 * @throws BrowserException
	 * @throws NoSuchRODAObjectException
	 *             if the object with the specified PID does not exist.
	 */
	public RODAObject getRODAObject(String pid) throws BrowserException,
			NoSuchRODAObjectException {

		Date start = new Date();

		RODAObject object = getBrowserHelper().getRODAObject(pid);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Browser.getRODAObject", new String[] { "pid", pid },
				"User %username% called method Browser.getRODAObject(" + pid
						+ ")", duration);

		return object;
	}

	/**
	 * Returns the number of {@link RODAObject}s that match the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of {@link RODAObject}s that
	 *         match the given {@link Filter}.
	 * 
	 * @throws BrowserException
	 */
	public int getRODAObjectCount(Filter filter) throws BrowserException {
		Date start = new Date();

		int count = getBrowserHelper().getRODAObjectCount(filter);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRODAObjectCount", new String[] { "filter",
				"" + filter },
				"User %username% called method Browser.getRODAObjectCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * Gets the list of {@link RODAObject}s that match the given
	 * {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link RODAObject} that match the given
	 *         {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public RODAObject[] getRODAObjects(ContentAdapter contentAdapter)
			throws BrowserException {
		Date start = new Date();

		List<RODAObject> simpleDOs = getBrowserHelper().getRODAObjects(
				contentAdapter);
		RODAObject[] objects = simpleDOs.toArray(new RODAObject[simpleDOs
				.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRODAObjects", new String[] {
				"contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getRODAObjects("
						+ contentAdapter + ")", duration);

		return objects;
	}

	/**
	 * @param pid
	 *            the PID of the Description Object.
	 * 
	 * @return the SimpleDescriptionObject for the given DO PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public SimpleDescriptionObject getSimpleDescriptionObject(String pid)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		SimpleDescriptionObject sdo = getBrowserHelper()
				.getSimpleDescriptionObject(pid);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleDescriptionObject", new String[] {
				"pid", pid },
				"User %username% called method Browser.getSimpleDescriptionObject("
						+ pid + ")", duration);

		return sdo;
	}

	/**
	 * Returns the number of {@link DescriptionObject}s that match the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of {@link DescriptionObject}s
	 *         that match the given {@link Filter}.
	 * 
	 * @throws BrowserException
	 */
	public int getSimpleDescriptionObjectCount(Filter filter)
			throws BrowserException {
		Date start = new Date();

		int count = getBrowserHelper().getSimpleDescriptionObjectCount(filter);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleDescriptionObjectCount", new String[] {
				"filter", "" + filter },
				"User %username% called method Browser.getSimpleDescriptionObjectCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * Gets the list of {@link SimpleDescriptionObject}s that match the given
	 * {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleDescriptionObject} that match the given
	 *         {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public SimpleDescriptionObject[] getSimpleDescriptionObjects(
			ContentAdapter contentAdapter) throws BrowserException {
		Date start = new Date();

		List<SimpleDescriptionObject> simpleDOs = getBrowserHelper()
				.getSimpleDescriptionObjects(contentAdapter);
		SimpleDescriptionObject[] sdos = simpleDOs
				.toArray(new SimpleDescriptionObject[simpleDOs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleDescriptionObjects", new String[] {
				"contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getSimpleDescriptionObjects("
						+ contentAdapter + ")", duration);
		return sdos;
	}

	/**
	 * Returns the index of given {@link DescriptionObject}'s pid within the
	 * list of {@link DescriptionObject}s for the specified
	 * {@link ContentAdapter}.
	 * 
	 * @param pid
	 *            the PID of the {@link DescriptionObject}.
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an <code>int</code> with the index of the given pid within the
	 *         list of results for the specified {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public int getSimpleDescriptionObjectIndex(String pid,
			ContentAdapter contentAdapter) throws BrowserException {
		Date start = new Date();

		int index = getBrowserHelper().getSimpleDescriptionObjectIndex(pid,
				contentAdapter);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleDescriptionObjectIndex", new String[] {
				"pid", "" + pid, "contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getSimpleDescriptionObjectIndex("
						+ pid + ", " + contentAdapter + ")", duration);

		return index;

	}

	/**
	 * @param pid
	 *            the PID of the Description Object.
	 * 
	 * @return the DescriptionObject for the given DO PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public DescriptionObject getDescriptionObject(String pid)
			throws BrowserException, NoSuchRODAObjectException {

		Date start = new Date();

		DescriptionObject dObject = getBrowserHelper()
				.getDescriptionObject(pid);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDescriptionObject", new String[] { "pid",
				pid },
				"User %username% called method Browser.getDescriptionObject("
						+ pid + ")", duration);

		return dObject;
	}

	/**
	 * Returns a list of all Description Object PIDs.
	 * 
	 * @return an array of {@link String} with the PIDs of all DOs.
	 * 
	 * @throws BrowserException
	 */
	public String[] getDOPIDs() throws BrowserException {
		Date start = new Date();

		List<String> doPIDs = getBrowserHelper().getDOPIDs();
		String[] pids = doPIDs.toArray(new String[doPIDs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDOPIDs", new String[0],
				"User %username% called method Browser.getDOPIDs()", duration);

		return pids;
	}

	/**
	 * Returns a list of ancestor PIDs for this Description Object.
	 * 
	 * @param pid
	 *            the PID of the Descriptive Object.
	 * 
	 * @return an array of {@link String} with the ancestor PIDs, from this DO
	 *         to the fonds DO.
	 * 
	 * @throws BrowserException
	 * @throws NoSuchRODAObjectException
	 */
	public String[] getDOAncestorPIDs(String pid) throws BrowserException,
			NoSuchRODAObjectException {
		Date start = new Date();

		List<String> ancestorPIDs = getBrowserHelper().getDOAncestorPIDs(pid);
		String[] pids = ancestorPIDs.toArray(new String[ancestorPIDs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDOAncestorPIDs",
				new String[] { "pid", pid },
				"User %username% called method Browser.getDOAncestorPIDs("
						+ pid + ")", duration);

		return pids;
	}

	/**
	 * Gets all the {@link RepresentationObject}s of a given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return an array of {@link RepresentationObject}s for the given DO PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public RepresentationObject[] getDORepresentations(String doPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationObject[] reps = getBrowserHelper().getDORepresentations(
				doPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDORepresentations", new String[] { "doPID",
				doPID },
				"User %username% called method Browser.getDORepresentations("
						+ doPID + ")", duration);

		return reps;
	}

	/**
	 * Gets the original {@link RepresentationObject} of a given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return the original {@link RepresentationObject} for the given DO PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public RepresentationObject getDOOriginalRepresentation(String doPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationObject ro = getBrowserHelper()
				.getDOOriginalRepresentation(doPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDOOriginalRepresentation", new String[] {
				"doPID", doPID },
				"User %username% called method Browser.getDOOriginalRepresentation("
						+ doPID + ")", duration);

		return ro;
	}

	/**
	 * Gets the normalised {@link RepresentationObject} of a given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return the normalized {@link RepresentationObject} for the given DO PID
	 *         or <code>null</code> if it doesn't exist.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public RepresentationObject getDONormalizedRepresentation(String doPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationObject ro = getBrowserHelper()
				.getDONormalizedRepresentation(doPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDONormalizedRepresentation", new String[] {
				"doPID", doPID },
				"User %username% called method Browser.getDONormalizedRepresentation("
						+ doPID + ")", duration);

		return ro;
	}

	/**
	 * Gets all the {@link RepresentationPreservationObject}s associated with
	 * the given {@link DescriptionObject} PID.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return an array of {@link RepresentationPreservationObject}
	 * 
	 * @throws BrowserException
	 * @throws NoSuchRODAObjectException
	 */
	public RepresentationPreservationObject[] getDOPreservationObjects(
			String doPID) throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationPreservationObject[] rpos = getBrowserHelper()
				.getDORepresentationPreservationObjects(doPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getDOPreservationObjects", new String[] {
				"doPID", doPID },
				"User %username% called method Browser.getDOPreservationObjects("
						+ doPID + ")", duration);

		return rpos;
	}

	/**
	 * Gets the {@link SimpleRepresentationObject} with the given PID.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * 
	 * @return a {@link SimpleRepresentationObject} for the given PID or
	 *         <code>null</code>.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public SimpleRepresentationObject getSimpleRepresentationObject(String roPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		SimpleRepresentationObject sro = getBrowserHelper()
				.getSimpleRepresentationObject(roPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleRepresentationObject", new String[] {
				"roPID", roPID },
				"User %username% called method Browser.getSimpleRepresentationObject("
						+ roPID + ")", duration);

		return sro;
	}

	/**
	 * Returns the number of {@link RepresentationObject}s that match the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link RepresentationObject}s that match the given {@link Filter}
	 *         .
	 * 
	 * @throws BrowserException
	 */
	public int getSimpleRepresentationObjectCount(Filter filter)
			throws BrowserException {
		Date start = new Date();

		int count = getBrowserHelper().getSimpleRepresentationObjectCount(
				filter);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleRepresentationObjectCount",
				new String[] { "filter", "" + filter },
				"User %username% called method Browser.getSimpleRepresentationObjectCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * Gets the list of {@link SimpleRepresentationObject}s that match the given
	 * {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleRepresentationObject} that match the
	 *         given {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public SimpleRepresentationObject[] getSimpleRepresentationObjects(
			ContentAdapter contentAdapter) throws BrowserException {
		Date start = new Date();

		List<SimpleRepresentationObject> simpleROs = getBrowserHelper()
				.getSimpleRepresentationObjects(contentAdapter);
		SimpleRepresentationObject[] sros = simpleROs
				.toArray(new SimpleRepresentationObject[simpleROs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleRepresentationObjects", new String[] {
				"contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getSimpleRepresentationObjects("
						+ contentAdapter + ")", duration);

		return sros;
	}

	/**
	 * Gets the {@link RepresentationObject} with the given PID.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * 
	 * @return a {@link RepresentationObject} for the given PID or
	 *         <code>null</code>.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public RepresentationObject getRepresentationObject(String roPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationObject rObject = getBrowserHelper()
				.getRepresentationObject(roPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRepresentationObject", new String[] {
				"roPID", roPID },
				"User %username% called method Browser.getRepresentationObject("
						+ roPID + ")", duration);

		return rObject;
	}

	/**
	 * Gets the {@link RepresentationFile} with the specified ID from specified
	 * the representation object.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * @param fileID
	 *            the {@link RepresentationFile} ID.
	 * 
	 * @return a {@link RepresentationFile} for the given PID and fileID or
	 *         <code>null</code>.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object doesn't exist.
	 * @throws NoSuchRepresentationFileException
	 *             if the specified representation file doesn't exist.
	 * @throws BrowserException
	 */
	public RepresentationFile getRepresentationFile(String roPID, String fileID)
			throws NoSuchRODAObjectException,
			NoSuchRepresentationFileException, BrowserException {
		Date start = new Date();

		RepresentationFile rFile = getBrowserHelper().getRepresentationFile(
				roPID, fileID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRepresentationFile", new String[] { "roPID",
				roPID, "fileID", fileID },
				"User %username% called method Browser.getRepresentationFile("
						+ roPID + ", " + fileID + ")", duration);

		return rFile;
	}

	/**
	 * Gets the {@link RepresentationPreservationObject} of a given
	 * {@link RepresentationObject}.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * 
	 * @return the {@link RepresentationPreservationObject} for the given RO PID
	 *         or <code>null</code> if the {@link RepresentationObject} doesn't
	 *         have a {@link RepresentationPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * @throws BrowserException
	 */
	public RepresentationPreservationObject getROPreservationObject(String roPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationPreservationObject po = getBrowserHelper()
				.getROPreservationObject(roPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getROPreservationObject", new String[] {
				"roPID", roPID },
				"User %username% called method Browser.getROPreservationObject("
						+ roPID + ")", duration);

		return po;
	}

	/**
	 * Gets the {@link SimpleRepresentationPreservationObject} with the given
	 * PID.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationPreservationObject}.
	 * 
	 * @return a {@link SimpleRepresentationPreservationObject} for the given
	 *         PID or <code>null</code>.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public SimpleRepresentationPreservationObject getSimpleRepresentationPreservationObject(
			String roPID) throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		SimpleRepresentationPreservationObject srpo = getBrowserHelper()
				.getSimpleRepresentationPreservationObject(roPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction(
				"Browser.getSimpleRepresentationPreservationObject",
				new String[] { "roPID", roPID },
				"User %username% called method Browser.getSimpleRepresentationPreservationObject("
						+ roPID + ")", duration);

		return srpo;
	}

	/**
	 * Returns the number of {@link RepresentationPreservationObject}s that
	 * match the given {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link RepresentationPreservationObject}s that match the given
	 *         {@link Filter} .
	 * 
	 * @throws BrowserException
	 */
	public int getSimpleRepresentationPreservationObjectCount(Filter filter)
			throws BrowserException {
		Date start = new Date();

		int count = getBrowserHelper()
				.getSimpleRepresentationPreservationObjectCount(filter);

		long duration = new Date().getTime() - start.getTime();
		registerAction(
				"Browser.getSimpleRepresentationPreservationObjectCount",
				new String[] { "filter", "" + filter },
				"User %username% called method Browser.getSimpleRepresentationPreservationObjectCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * Gets the list of {@link SimpleRepresentationPreservationObject}s that
	 * match the given {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleRepresentationPreservationObject} that
	 *         match the given {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public SimpleRepresentationPreservationObject[] getSimpleRepresentationPreservationObjects(
			ContentAdapter contentAdapter) throws BrowserException {
		Date start = new Date();

		List<SimpleRepresentationPreservationObject> simpleROs = getBrowserHelper()
				.getSimpleRepresentationPreservationObjects(contentAdapter);
		SimpleRepresentationPreservationObject[] srpos = simpleROs
				.toArray(new SimpleRepresentationPreservationObject[simpleROs
						.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction(
				"Browser.getSimpleRepresentationPreservationObjects",
				new String[] { "contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getSimpleRepresentationPreservationObjects("
						+ contentAdapter + ")", duration);

		return srpos;
	}

	/**
	 * Gets the specified {@link RepresentationPreservationObject}.
	 * 
	 * @param poPID
	 *            the PID of the {@link RepresentationPreservationObject}.
	 * 
	 * @return the {@link RepresentationPreservationObject} with the given PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * @throws BrowserException
	 */
	public RepresentationPreservationObject getRepresentationPreservationObject(
			String poPID) throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RepresentationPreservationObject rpo = getBrowserHelper()
				.getRepresentationPreservationObject(poPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRepresentationPreservationObject",
				new String[] { "poPID", poPID },
				"User %username% called method Browser.getRepresentationPreservationObject("
						+ poPID + ")", duration);

		return rpo;
	}

	/**
	 * Gets the {@link SimpleEventPreservationObject} with the given PID.
	 * 
	 * @param roPID
	 *            the PID of the {@link EventPreservationObject}.
	 * 
	 * @return a {@link SimpleEventPreservationObject} for the given PID or
	 *         <code>null</code>.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public SimpleEventPreservationObject getSimpleEventPreservationObject(
			String roPID) throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		SimpleEventPreservationObject event = getBrowserHelper()
				.getSimpleEventPreservationObject(roPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleEventPreservationObject",
				new String[] { "roPID", roPID },
				"User %username% called method Browser.getSimpleEventPreservationObject("
						+ roPID + ")", duration);
		return event;
	}

	/**
	 * Returns the number of {@link EventPreservationObject}s that match the
	 * given {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link EventPreservationObject}s that match the given
	 *         {@link Filter} .
	 * 
	 * @throws BrowserException
	 */
	public int getSimpleEventPreservationObjectCount(Filter filter)
			throws BrowserException {
		Date start = new Date();

		int count = getBrowserHelper().getSimpleEventPreservationObjectCount(
				filter);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleEventPreservationObjectCount",
				new String[] { "filter", "" + filter },
				"User %username% called method Browser.getSimpleEventPreservationObjectCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * Gets the list of {@link SimpleEventPreservationObject}s that match the
	 * given {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleEventPreservationObject} that match the
	 *         given {@link ContentAdapter}.
	 * 
	 * @throws BrowserException
	 */
	public SimpleEventPreservationObject[] getSimpleEventPreservationObjects(
			ContentAdapter contentAdapter) throws BrowserException {
		Date start = new Date();

		List<SimpleEventPreservationObject> simpleROs = getBrowserHelper()
				.getSimpleEventPreservationObjects(contentAdapter);
		SimpleEventPreservationObject[] events = simpleROs
				.toArray(new SimpleEventPreservationObject[simpleROs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getSimpleEventPreservationObjects",
				new String[] { "contentAdapter", "" + contentAdapter },
				"User %username% called method Browser.getSimpleEventPreservationObjects("
						+ contentAdapter + ")", duration);

		return events;
	}

	/**
	 * Gets the specified {@link EventPreservationObject}.
	 * 
	 * @param poPID
	 *            the PID of the {@link EventPreservationObject}.
	 * 
	 * @return the {@link EventPreservationObject} with the given PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public EventPreservationObject getEventPreservationObject(String poPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		EventPreservationObject event = getBrowserHelper()
				.getEventPreservationObject(poPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getEventPreservationObject", new String[] {
				"poPID", poPID },
				"User %username% called method Browser.getEventPreservationObject("
						+ poPID + ")", duration);

		return event;
	}

	/**
	 * Gets all the {@link EventPreservationObject}s for the the specified
	 * {@link RepresentationPreservationObject}s PID.
	 * 
	 * @param poPID
	 *            the PID of the {@link RepresentationPreservationObject}.
	 * 
	 * @return an array of {@link EventPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public EventPreservationObject[] getPreservationEvents(String poPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		List<EventPreservationObject> eventPOs = getBrowserHelper()
				.getPreservationEventsPerformedOn(poPID);
		EventPreservationObject[] events = eventPOs
				.toArray(new EventPreservationObject[eventPOs.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getPreservationEvents", new String[] { "poPID",
				poPID },
				"User %username% called method Browser.getPreservationEvents("
						+ poPID + ")", duration);
		return events;
	}

	/**
	 * Gets the specified {@link AgentPreservationObject}.
	 * 
	 * @param poPID
	 *            the PID of the {@link AgentPreservationObject}.
	 * 
	 * @return the {@link AgentPreservationObject} with the given PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified object PID doesn't exist.
	 * 
	 * @throws BrowserException
	 */
	public AgentPreservationObject getAgentPreservationObject(String poPID)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		AgentPreservationObject agent = getBrowserHelper()
				.getAgentPreservationObject(poPID);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getAgentPreservationObject", new String[] {
				"poPID", poPID },
				"User %username% called method Browser.getAgentPreservationObject("
						+ poPID + ")", duration);

		return agent;
	}

	/**
	 * Gets the {@link RODAObjectPermissions} for the specified
	 * {@link RODAObject}.
	 * 
	 * @param pid
	 *            the {@link RODAObject} PID.
	 * 
	 * @return the {@link RODAObjectPermissions}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public RODAObjectPermissions getRODAObjectPermissions(String pid)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RODAObjectPermissions objectPermissions = getBrowserHelper()
				.getRODAObjectPermissions(pid);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRODAObjectPermissions", new String[] {
				"pid", pid },
				"User %username% called method Browser.getRODAObjectPermissions("
						+ pid + ")", duration);

		return objectPermissions;
	}

	/**
	 * Gets the {@link RODAObjectUserPermissions} for the current user over the
	 * specified {@link RODAObject}.
	 * 
	 * @param pid
	 *            the {@link RODAObject} PID.
	 * 
	 * @return the {@link RODAObjectUserPermissions}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public RODAObjectUserPermissions getRODAObjectUserPermissions(String pid)
			throws BrowserException, NoSuchRODAObjectException {
		Date start = new Date();

		RODAObjectUserPermissions objectUserPermissions = getBrowserHelper()
				.getRODAObjectUserPermissions(pid, getClientUser());

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.getRODAObjectUserPermissions", new String[] {
				"pid", pid },
				"User %username% called method Browser.getRODAObjectUserPermissions("
						+ pid + ")", duration);

		return objectUserPermissions;
	}

	/**
	 * Verifies if the current user has write permission over the specified
	 * object.
	 * 
	 * @param pid
	 *            the PID of the {@link RODAObject}.
	 * 
	 * @return <code>true</code> if the current user has write permission and
	 *         <code>false</code> otherwise.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public boolean hasModifyPermission(String pid) throws BrowserException,
			NoSuchRODAObjectException {
		Date start = new Date();

		boolean result = getBrowserHelper().hasModifyPermission(pid,
				getClientUser());

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.hasModifyPermission",
				new String[] { "pid", pid },
				"User %username% called method Browser.hasModifyPermission("
						+ pid + ")", duration);

		return result;
	}

	/**
	 * Verifies if the current user has remove permission over the specified
	 * object.
	 * 
	 * @param pid
	 *            the PID of the {@link RODAObject}.
	 * 
	 * @return <code>true</code> if the current user has remove permission and
	 *         <code>false</code> otherwise.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public boolean hasRemovePermission(String pid) throws BrowserException,
			NoSuchRODAObjectException {
		Date start = new Date();

		boolean result = getBrowserHelper().hasRemovePermission(pid,
				getClientUser());

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.hasRemovePermission",
				new String[] { "pid", pid },
				"User %username% called method Browser.hasRemovePermission("
						+ pid + ")", duration);

		return result;
	}

	/**
	 * Verifies of the current user has grant permission over the specified
	 * object.
	 * 
	 * @param pid
	 *            the PID of the {@link RODAObject}.
	 * 
	 * @return <code>true</code> if the current user has grant permission and
	 *         <code>false</code> otherwise.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public boolean hasGrantPermission(String pid) throws BrowserException,
			NoSuchRODAObjectException {
		Date start = new Date();

		boolean result = getBrowserHelper().hasGrantPermission(pid,
				getClientUser());

		long duration = new Date().getTime() - start.getTime();
		registerAction("Browser.hasGrantPermission",
				new String[] { "pid", pid },
				"User %username% called method Browser.hasGrantPermission("
						+ pid + ")", duration);

		return result;
	}

	/**
	 * Gets the {@link Producers} for a Fonds.
	 * 
	 * @param doPID
	 *            the PID of the Fonds {@link DescriptionObject} or any of it's
	 *            descendants.
	 * 
	 * @return the {@link Producers}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws BrowserException
	 */
	public Producers getProducers(String doPID)
			throws NoSuchRODAObjectException, BrowserException {
		Date start = new Date();

		Producers producers = getBrowserHelper().getProducers(doPID);

		long duration = new Date().getTime() - start.getTime();

		registerAction("Browser.getProducers", new String[] { "doPID", doPID },
				"User %username% called method Browser.getProducers(" + doPID
						+ ")", duration);

		return producers;
	}

	private BrowserHelper getBrowserHelper() throws BrowserException {

		User clientUser = getClientUser();

		if (clientUser != null) {

			String usernamePasswordKey = clientUser.getName()
					+ getClientUserPassword();

			if (!this.browserHelperCache.containsKey(usernamePasswordKey)) {

				try {

					FedoraClientUtility fedoraClient = null;

					String fedoraURL = getConfiguration()
							.getString("fedoraURL");
					String fedoraGSearchURL = getConfiguration().getString(
							"fedoraGSearchURL");

					fedoraClient = new FedoraClientUtility(fedoraURL,
							fedoraGSearchURL, clientUser,
							getClientUserPassword());

					this.browserHelperCache
							.put(usernamePasswordKey, new BrowserHelper(
									fedoraClient, getConfiguration()));

				} catch (MalformedURLException e) {
					logger.error("Error creating BrowserHelper - "
							+ e.getMessage(), e);
					throw new BrowserException(
							"Error creating BrowserHelper - " + e.getMessage(),
							e);
				} catch (FedoraClientException e) {
					logger.error("Error creating Fedora client - "
							+ e.getMessage(), e);
					throw new BrowserException(
							"Error creating Fedora client - " + e.getMessage(),
							e);
				}

			}

			return this.browserHelperCache.get(usernamePasswordKey);

		} else {

			throw new BrowserException("User credentials are not available.");
		}

	}

}
