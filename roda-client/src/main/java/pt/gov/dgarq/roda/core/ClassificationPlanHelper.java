package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelManager;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCHelper;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * 
 */
public class ClassificationPlanHelper {

	private static final String EADC_FILE_NAME = "EADC.xml";

	private static final FileFilter DIRECTORY_FILTER = new FileFilter() {

		public boolean accept(File file) {
			return file.isDirectory();
		}

	};

	private static final Logger logger = Logger
			.getLogger(ClassificationPlanHelper.class);

	/**
	 * Classification plan listener interface
	 * 
	 */
	public interface ClassificationPlanListener {
		/**
		 * Called when updating a description object
		 * 
		 * @param id
		 */
		public void onUpdate(String id);
	}

	private final File workDir;
	private final File tmpDir;

	private final List<ClassificationPlanListener> listeners;

	/**
	 * Create a new classification plan
	 * 
	 * @param workDir
	 *            the classification plan working directory
	 * @param tmpDir
	 *            the temporary directory to use
	 */
	public ClassificationPlanHelper(File workDir, File tmpDir) {
		this.workDir = workDir;
		this.tmpDir = tmpDir;
		if (!workDir.exists()) {
			workDir.mkdir();
		}
		listeners = new ArrayList<ClassificationPlanListener>();
	}

	/**
	 * Update classification plan
	 * 
	 * @param rodaClient
	 * @throws IOException
	 * @throws RODAClientException
	 * @throws BrowserException
	 * @throws EadCMetadataException
	 * @throws MetadataException
	 * @throws InvalidDescriptionObjectException
	 * @throws NoSuchRODAObjectException
	 * 
	 */
	public void update(RODAClient rodaClient) throws IOException,
			RODAClientException, BrowserException, NoSuchRODAObjectException,
			InvalidDescriptionObjectException, MetadataException,
			EadCMetadataException {
		logger.debug("Creating temporary directory for classification plan");
		File tempDir = TempDir.createUniqueTemporaryDirectory("ead", tmpDir);

		logger.debug("Downloading classification plan to temporary directory");
		Browser browser = rodaClient.getBrowserService();
		Filter filter = new Filter();
		filter.add(SimpleDescriptionObject.FONDS_FILTER.getParameters());
		filter.add(new ProducerFilterParameter());
		SimpleDescriptionObject[] collections = browser
				.getSimpleDescriptionObjects(new ContentAdapter(filter, null,
						null));
		if (collections == null) {
			collections = new SimpleDescriptionObject[] {};
		}
		for (SimpleDescriptionObject sdo : collections) {
			exportDescriptionObject(sdo, tempDir, browser);
		}

		logger.debug("Moving temporary directory to working directory");
		FileUtils.deleteDirectory(workDir);
		FileUtils.moveDirectory(tempDir, workDir);

	}

	private void exportDescriptionObject(SimpleDescriptionObject sdo,
			File parentDir, Browser browser) throws BrowserException,
			NoSuchRODAObjectException, IOException,
			InvalidDescriptionObjectException, MetadataException,
			EadCMetadataException {

		onUpdate(sdo.getId());
		File sdoDir = new File(parentDir.getAbsolutePath() + File.separator
				+ pid2fileName(sdo.getPid()));
		if (sdoDir.mkdir()) {
			DescriptionObject dObj = browser.getDescriptionObject(sdo.getPid());
			// Create EAD
			File eadcFile = new File(sdoDir, EADC_FILE_NAME);

			new EadCHelper(dObj).saveToFile(eadcFile);

			// if SDO can have children greater than document level
			if (DescriptionLevelManager
					.getAllButRepresentationsDescriptionLevels().contains(
							sdo.getLevel())) {

				ContentAdapter adapter = new ContentAdapter();
				Filter filter = new Filter();
				filter.add(new ProducerFilterParameter());
				int size = DescriptionLevelManager
						.getAllButRepresentationsDescriptionLevels().size();
				String[] levels = new String[size];
				for (int i = 0; i < size; i++) {
					levels[i] = DescriptionLevelManager
							.getAllButRepresentationsDescriptionLevels().get(i)
							.getLevel();
				}
				filter.add(new OneOfManyFilterParameter(
						SimpleDescriptionObject.LEVEL, levels));
				filter.add(new SimpleFilterParameter("parentPID", sdo.getPid()));
				adapter.setFilter(filter);

				// Get children
				SimpleDescriptionObject[] subElements = browser
						.getSimpleDescriptionObjects(adapter);

				// Recursively export children if greater than document level
				if (subElements != null) {
					for (SimpleDescriptionObject child : subElements) {
						if (DescriptionLevelManager
								.getAllButRepresentationsDescriptionLevels()
								.contains(child.getLevel())) {
							exportDescriptionObject(child, sdoDir, browser);
						}
					}
				}
			}

		} else {
			throw new IOException("Could not create directory "
					+ sdoDir.getAbsolutePath());
		}

	}

	private String pid2fileName(String pid) {
		return pid.substring(pid.indexOf(':') + 1);
	}

	private String fileName2Pid(String fileName) {
		return "roda:" + fileName;
	}

	/**
	 * Get all collections (fonds)
	 * 
	 * @return a list with the collections description objects
	 */
	public List<DescriptionObject> getCollections() {
		return getDescriptionObjects(workDir);
	}

	/**
	 * Get element's sub elements
	 * 
	 * @param pid
	 *            the pid of an element
	 * @return a list with the description objects of the elements
	 */
	public List<DescriptionObject> getSubElements(String pid) {
		return getDescriptionObjects(lookup(pid));
	}

	private File lookup(String pid) {
		return lookup(pid, workDir);
	}

	private File lookup(String pid, File dir) {
		File lookupDir = null;
		for (File subDir : dir.listFiles(DIRECTORY_FILTER)) {
			if (subDir.getName().equals(pid2fileName(pid))) {
				lookupDir = subDir;
				break;
			} else {
				lookupDir = lookup(pid, subDir);
				if (lookupDir != null) {
					break;
				}
			}
		}
		return lookupDir;
	}

	/**
	 * Get an element ancestors
	 * 
	 * @param pid
	 * @return A list with the path to this element, inclusive, or null if no
	 *         path found
	 * @throws IOException
	 * @throws EadCMetadataException
	 */
	public List<DescriptionObject> getAncestors(String pid) throws IOException,
			EadCMetadataException {
		List<DescriptionObject> ancestors = getAncestors(pid, workDir);
		return ancestors.size() != 0 ? ancestors : null;
	}

	private List<DescriptionObject> getAncestors(String pid, File dir)
			throws IOException, EadCMetadataException {
		List<DescriptionObject> ancestors = new ArrayList<DescriptionObject>();
		for (File subDir : dir.listFiles(DIRECTORY_FILTER)) {
			if (subDir.getName().equals(pid2fileName(pid))) {
				ancestors.add(getDescriptionObject(subDir));
				break;
			} else {
				List<DescriptionObject> subAncestors = getAncestors(pid, subDir);
				if (subAncestors.size() > 0) {
					ancestors.add(getDescriptionObject(subDir));
					ancestors.addAll(subAncestors);
					break;
				}
			}
		}
		return ancestors;
	}

	/**
	 * Get a classification plan object complete reference
	 * 
	 * @param pid
	 *            classification plan object PID
	 * @return the complete reference, ending with a backslash
	 * 
	 * @throws IOException
	 * @throws EadCMetadataException
	 */
	public String getCompleteReference(String pid) throws IOException,
			EadCMetadataException {
		String ret = null;
		if (pid != null) {
			List<DescriptionObject> ancestors = getAncestors(pid);
			if (ancestors != null) {
				ret = "";
				for (DescriptionObject descObj : getAncestors(pid)) {
					if (ret.length() > 0) {
						ret += "/";
					}
					ret += descObj.getId();
				}
			}
		}
		return ret;
	}

	private List<DescriptionObject> getDescriptionObjects(File dir) {
		List<DescriptionObject> objs = new ArrayList<DescriptionObject>();
		if (dir.isDirectory()) {

			for (File subDir : dir.listFiles(DIRECTORY_FILTER)) {
				try {
					objs.add(getDescriptionObject(subDir));
				} catch (IOException e) {
					logger.error("Error getting description object on "
							+ subDir.getAbsolutePath(), e);
				} catch (EadCMetadataException e) {
					logger.error("Error getting description object on "
							+ subDir.getAbsolutePath(), e);
				}
			}
		}
		return objs;
	}

	private DescriptionObject getDescriptionObject(File dir)
			throws EadCMetadataException, FileNotFoundException, IOException {

		DescriptionObject ret = null;
		File eadCFile = new File(dir.getAbsolutePath() + File.separator
				+ EADC_FILE_NAME);
		if (eadCFile.exists()) {

			ret = EadCHelper.newInstance(eadCFile).getDescriptionObject();
			ret.setPid(fileName2Pid(dir.getName()));

		} else {
			logger.error("Could not found EAD-C file "
					+ "when parsing description object from "
					+ dir.getAbsolutePath());
		}
		return ret;
	}

	/**
	 * Get description object
	 * 
	 * @param pid
	 * @return the description object or null if not found
	 * @throws IOException
	 * @throws EadCMetadataException
	 */
	public DescriptionObject getDescriptionObject(String pid)
			throws IOException, EadCMetadataException {
		DescriptionObject ret = null;
		File dir = lookup(pid);
		if (dir != null) {
			ret = getDescriptionObject(dir);
		}
		return ret;
	}

	/**
	 * Add a classification plan listener
	 * 
	 * @param listener
	 */
	public void addClassificationPlanListener(
			ClassificationPlanListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a classification plan listener
	 * 
	 * @param listener
	 */
	public void removeClassificationPlanListener(
			ClassificationPlanListener listener) {
		listeners.remove(listener);
	}

	protected void onUpdate(String id) {
		for (ClassificationPlanListener listener : listeners) {
			listener.onUpdate(id);
		}
	}

}
