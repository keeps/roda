package pt.gov.dgarq.roda.core.ingest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SIPStateTransition;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * @author Rui Castro
 */
public class IngestManager {
	private static final Logger logger = Logger.getLogger(IngestManager.class);

	public static File RODA_HOME = null;
	public static File RODA_CORE_CONFIG_DIRECTORY = null;

	private static IngestManager instance = null;

	/**
	 * Returns the current instance of the {@link IngestManager}.
	 * 
	 * @return an instance of an {@link IngestManager}.
	 * @throws IngestRegistryException
	 */
	public synchronized static IngestManager getDefaultIngestManager()
			throws IngestRegistryException {
		if (instance == null) {
			instance = new IngestManager();
		}
		return instance;
	}

	private Configuration configuration = null;

	private String location = null;

	private File ftpDropDirectory = null;

	private String quarantineState = null;

	private List<String> states = null;

	private List<String> initialStates = null;

	private List<String> finalStates = null;

	private IngestDatabaseUtility databaseUtility = null;

	private IngestManager() throws IngestRegistryException {

		if (System.getProperty("roda.home") != null) {
			RODA_HOME = new File(System.getProperty("roda.home"));
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
		} else {
			RODA_HOME = new File(".");
		}

		RODA_CORE_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

		if (configuration == null) {

			try {

				this.configuration = getConfiguration("ingest.properties");

				location = this.configuration.getString("location", location);

				ftpDropDirectory = new File(this.configuration
						.getString("ftpDropDirectory"));

				if (this.configuration.containsKey("quarantine")) {
					quarantineState = this.configuration
							.getString("quarantine");
				} else {
					// No states
					throw new ConfigurationException(
							"quarantine is not in configuration");
				}

				if (this.configuration.containsKey("states")) {
					states = Arrays.asList(this.configuration
							.getStringArray("states"));
				} else {
					// No states
					throw new ConfigurationException(
							"states are not listed in configuration");
				}

				if (this.configuration.containsKey("initialStates")) {
					initialStates = Arrays.asList(this.configuration
							.getStringArray("initialStates"));
				} else {
					// No initial states
					throw new ConfigurationException(
							"initialStates are not listed in configuration");
				}

				if (this.configuration.containsKey("finalStates")) {
					finalStates = Arrays.asList(this.configuration
							.getStringArray("finalStates"));

					// Quarantine state is ALWAYS one of the final states.
					if (!finalStates.contains(quarantineState)) {
						finalStates.add(quarantineState);
					}

				} else {
					// No final states
					throw new ConfigurationException(
							"finalStates are not listed in configuration");
				}
			} catch (ConfigurationException e) {
				logger.error("error reading configuration file", e);
				throw new IngestRegistryException(
						"error reading configuration file", e);
			}

		}

		if (databaseUtility == null) {

			String jdbcDriver = this.configuration.getString("jdbcDriver");
			String jdbcURL = this.configuration.getString("jdbcURL");
			String jdbcUsername = this.configuration.getString("jdbcUsername");
			String jdbcPassword = this.configuration.getString("jdbcPassword");

			databaseUtility = new IngestDatabaseUtility(jdbcDriver, jdbcURL,
					jdbcUsername, jdbcPassword);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Return the configuration properties with the specified name.
	 * 
	 * @param configurationFile
	 *            the name of the configuration file.
	 * 
	 * @return a {@link Configuration} with the properties of the specified
	 *         name.
	 * 
	 * @throws ConfigurationException
	 */
	private Configuration getConfiguration(String configurationFile)
			throws ConfigurationException {

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(false);

		File externalConfigurationFile = new File(RODA_CORE_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile));
			logger.debug("Loading default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

	/**
	 * @return the databaseUtility
	 */
	public IngestDatabaseUtility getDatabaseUtility() {
		return databaseUtility;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the quarantine
	 */
	public String getQuarantineState() {
		return quarantineState;
	}

	/**
	 * @return the states
	 */
	public List<String> getStates() {
		return new ArrayList<String>(states);
	}

	/**
	 * @return the initialStates
	 */
	public List<String> getInitialStates() {
		return new ArrayList<String>(initialStates);
	}

	/**
	 * @return the finalStates
	 */
	public List<String> getFinalStates() {
		return new ArrayList<String>(finalStates);
	}

	/**
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Checks if the given state is a final state.
	 * 
	 * @param state
	 *            the state to check.
	 * 
	 * @return <code>true</code> if <code>state</code> is one of the final
	 *         states and <code>false</code> otherwise.
	 */
	public boolean isStateFinal(String state) {
		return getFinalStates().contains(state);
	}

	/**
	 * Reads the inital state for class <code>c</code> from the configuration
	 * file.
	 * 
	 * @param c
	 *            the class
	 * @return the inital state for class <code>c</code> or <code>null</code> if
	 *         is not found in configuration file.
	 */
	public String getInitialState(Class<? extends IngestTask> c) {
		String key = c.getName() + ".initialState";
		return getConfiguration().getString(key, null);
	}

	/**
	 * Reads the final state for class <code>c</code> from the configuration
	 * file.
	 * 
	 * @param c
	 *            the class
	 * @return the final state for class <code>c</code> or <code>null</code> if
	 *         is not found in configuration file.
	 */
	public String getFinalState(Class<? extends IngestTask> c) {
		String key = c.getName() + ".finalState";
		return getConfiguration().getString(key, null);
	}

	/**
	 * @param sip
	 * @return
	 */
	public File getCurrentSIPLocation(SIPState sip) {
		return getLocationForState(sip, sip.getState());
	}

	/**
	 * @param sip
	 * @param state
	 * @return
	 */
	public File getLocationForState(SIPState sip, String state) {
		File stateDir = new File(getLocation(), state);
		File userStateDir = new File(stateDir, sip.getUsername());
		return new File(userStateDir, sip.getId());
	}

	/**
	 * @param sipID
	 * @return
	 * @throws NoSuchSIPException
	 * @throws IngestRegistryException
	 */
	public SIPState getSIP(String sipID) throws NoSuchSIPException,
			IngestRegistryException {
		return getDatabaseUtility().getSIPState(sipID);
	}

	/**
	 * @param state
	 * @return
	 * @throws IngestRegistryException
	 */
	public List<SIPState> getSIPsInState(String state)
			throws IngestRegistryException {

		Filter filterState = new Filter(new SimpleFilterParameter("state",
						state));

		return getDatabaseUtility().getSIPStates(
				new ContentAdapter(filterState, null, null));
	}

	/**
	 * Returns the number of {@link SIPState}s that respect the specified
	 * filters.
	 * 
	 * @param contentAdapterFilter
	 * 
	 * @return the number of {@link SIPState}s.
	 * 
	 * @throws IngestRegistryException
	 */
	public int getSIPsCount(Filter contentAdapterFilter)
			throws IngestRegistryException {
		return getDatabaseUtility().getSIPStateCount(contentAdapterFilter);
	}

	/**
	 * * Returns a list of {@link SIPState}s matching the {@link ContentAdapter}
	 * specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link SIPState}.
	 * 
	 * @throws IngestRegistryException
	 */
	public List<SIPState> getSIPs(ContentAdapter contentAdapter)
			throws IngestRegistryException {
		return getDatabaseUtility().getSIPStates(contentAdapter);
	}

	/**
	 * Activates the processing flag of the specified {@link SIPState}.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState}.
	 * 
	 * @throws NoSuchSIPException
	 *             if the specified {@link SIPState} deson't exist.
	 * @throws SIPAlreadyProcessingException
	 *             if the processing flag is already on.
	 * @throws IngestRegistryException
	 *             if any other error occurs.
	 */
	public void activateProcessingFlag(String sipID) throws NoSuchSIPException,
			SIPAlreadyProcessingException, IngestRegistryException {

		getDatabaseUtility().activateProcessingFlag(sipID);

	}

	/**
	 * Activates the processing flag of the specified {@link SIPState}.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState}.
	 * 
	 * @return a <code>boolean</code> with the value of the processing flag
	 *         before deactivation. <strong>NOTE:</strong> a value of
	 *         <code>false</code> could mean the flag was off or that the
	 *         {@link SIPState} doesn't exist in the database.
	 * 
	 * @throws IngestRegistryException
	 *             if any other error occurs.
	 */
	public boolean deactivateProcessingFlag(String sipID)
			throws IngestRegistryException {

		return getDatabaseUtility().deactivateProcessingFlag(sipID);

	}

	/**
	 * 
	 * @param sip
	 * @return
	 */
	public String[] getStatesToFinish(SIPState sip) {

		if (sip.isComplete()) {
			return new String[0];
		} else {

			SIPStateTransition[] stateTransitions = sip.getStateTransitions();
			List<String> passedStates = new ArrayList<String>();

			for (int i = 0; i < stateTransitions.length; i++) {
				passedStates.add(stateTransitions[i].getToState());
			}

			List<String> statesToFinish = getStates();
			statesToFinish.removeAll(passedStates);

			return statesToFinish.toArray(new String[statesToFinish.size()]);
		}

	}

	/**
	 * Deactivate the processing flag for ALL SIPs.
	 * 
	 * <p>
	 * <strong>ATTENTION:</strong> this method should be used with care. It
	 * should only be called when the caller is certain that there are no SIPs
	 * being processed, like before the ingest tasks are initiated.
	 * </p>
	 * 
	 * @throws IngestRegistryException
	 */
	public void clearProcessingFlags() throws IngestRegistryException {

		logger.info("Clearing processing flags of ALL SIPs");

		getDatabaseUtility().clearProcessingFlags();
	}

	public void createFTPDropDirectories(User[] users) {

		logger.info("Creating FTP user directories");

		if (users != null) {
			for (User user : users) {

				File userDropDirectory = new File(this.ftpDropDirectory, user
						.getName());

				if (userDropDirectory.exists()) {

					if (!userDropDirectory.isDirectory()) {
						logger.warn("FTP drop directory for user "
								+ user.getName() + " is not a directory");
					}

				} else {

					if (userDropDirectory.mkdir()) {
						logger.info("Created FTP drop directory for user "
								+ user.getName());
					} else {
						logger
								.error("Error creating FTP drop directory for user "
										+ user.getName());
					}
				}
			}
		}

	}

	protected SIPState insertSIP(String username, String originalFilename)
			throws IngestRegistryException {
		return getDatabaseUtility().insertSIPState(username, originalFilename);
	}

	protected void registerStateChange(String sipID, String initialState,
			String finalState, String ingestedPID, String parentPID,
			String taskID, boolean success, String description)
			throws IngestRegistryException {

		try {

			SIPState sip = getSIP(sipID);

			boolean isComplete = isStateFinal(finalState);
			float completePercentage = 0;

			if (isComplete) {

				completePercentage = 100;

			} else {

				List<String> states2 = getStates();
				states2.removeAll(getInitialStates());
				states2.removeAll(getFinalStates());

				// +1 initial state
				int numberOfStates = states2.size() + 1;

				SIPStateTransition[] stateTransitions = sip
						.getStateTransitions();
				// +1 because we're registering a new state transition
				int completedStates = (stateTransitions != null) ? stateTransitions.length + 1
						: 0;
				completePercentage = completedStates * 100 / numberOfStates;
			}

			getDatabaseUtility().insertStateTransition(sipID, initialState,
					finalState, isStateFinal(finalState), completePercentage,
					ingestedPID, parentPID, taskID, success, description);

		} catch (NoSuchSIPException e) {
			throw new IngestRegistryException("Error getting SIPState for SIP "
					+ sipID + " - " + e.getMessage(), e);
		}

	}

	protected void removeSIP(String sipID) throws IngestRegistryException,
			NoSuchSIPException {
		getDatabaseUtility().removeSIPState(sipID);
	}

}
