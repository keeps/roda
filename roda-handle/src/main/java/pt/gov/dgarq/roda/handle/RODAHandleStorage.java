package pt.gov.dgarq.roda.handle;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleStorage;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ScanCallback;
import net.handle.hdllib.Util;
import net.handle.util.StreamTable;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.stubs.Browser;

/**
 * This class is a {@link HandleStorage} for RODA. It does not store handles,
 * but it can get information for handles.
 * 
 * @author Rui Castro
 */
public class RODAHandleStorage implements HandleStorage {

	static final private Logger logger = Logger
			.getLogger(RODAHandleStorage.class);

	private File roda_config_directory;

	private URL rodacoreURL;
	private String rodaUsername;
	private String rodaPassword;

	private String rodaUrlPrefix = null;

	private String handleRootNA = null;
	private String rodaHandlePrefix = null;

	private String rodaNAHandle = null;
	private Browser browserService = null;

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#init(StreamTable)
	 */
	public void init(StreamTable configTable) throws Exception {
		String roda_home;
		if (System.getProperty("roda.home") != null) {
			roda_home = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			roda_home = System.getenv("RODA_HOME");
		} else {
			throw new Exception("RODA home is not defined!");
		}

		roda_config_directory = new File(roda_home, "config");
		try {
			Configuration configuration = getConfiguration("roda-handle.properties");

			rodacoreURL = new URL(configuration.getString("rodacoreURL"));
			rodaUsername = configuration.getString("rodaUsername");
			rodaPassword = configuration.getString("rodaPassword");

			handleRootNA = configuration.getString("handleRootNA");
			rodaHandlePrefix = configuration.getString("rodaHandlePrefix");
			rodaUrlPrefix = configuration.getString("rodaUrlPrefix");

		} catch (ConfigurationException e) {
			logger.error(
					"Error reading configuration file - roda-handle.properties - "
							+ e.getMessage(), e);
			throw new Exception(
					"Error reading configuration file - roda-handle.properties - "
							+ e.getMessage(), e);
		}

		rodaNAHandle = handleRootNA + "/" + rodaHandlePrefix;
		RODAClient rodaClient = new RODAClient(rodacoreURL, rodaUsername,
				rodaPassword);

		browserService = rodaClient.getBrowserService();

		logger.info("init OK");
	}

	/**
	 * @see HandleStorage#shutdown()
	 */
	public void shutdown() {
		logger.info("shutdown OK");
	}

	/**
	 * Scan the database for handles with the given naming authority and return
	 * an Enumeration of byte arrays with each byte array being a handle.
	 * 
	 * @see HandleStorage#getHandlesForNA(byte[])
	 */
	public Enumeration getHandlesForNA(byte[] naHdl) throws HandleException {

		String naHandle = Util.decodeString(naHdl);

		logger.info("Called getHandlesForNA for NA " + naHandle);

		try {

			if (rodaNAHandle.equals(naHandle)) {

				// Get the PIDs of all Description Objects and return 1 handle
				// for each PID.

				List<byte[]> handles = new ArrayList<byte[]>();

				for (String doPID : browserService.getDOPIDs()) {
					handles.add(Util.encodeString(getHandleForPID(doPID)));
				}

				return Collections.enumeration(handles);

			} else {
				logger.error("Handle " + naHandle + " is not RODA Handle NA.");
				throw new HandleException(HandleException.INVALID_VALUE,
						"Handle " + naHandle + " is not RODA Handle NA.");
			}

		} catch (BrowserException e) {
			logger.error("Error getting DO PIDs - " + e.getMessage(), e);
			throw new HandleException(HandleException.INTERNAL_ERROR,
					"Error getting DO PIDs - " + e.getMessage());
		} catch (RemoteException e) {
			RODAException parsedRemoteException = RODAClient
					.parseRemoteException(e);
			logger.error(
					"Error getting DO PIDs - "
							+ parsedRemoteException.getMessage(),
					parsedRemoteException);
			throw new HandleException(HandleException.INTERNAL_ERROR,
					"Error getting DO PIDs - "
							+ parsedRemoteException.getMessage());
		}

	}

	/**
	 * Return the pre-packaged values of the given handle that are either in the
	 * indexList or the typeList.
	 * 
	 * @see HandleStorage#getRawHandleValues(byte[], int[], byte[][])
	 */
	public byte[][] getRawHandleValues(byte[] handle, int[] indexList,
			byte[][] typeList) throws HandleException {

		String decodedHandle = Util.decodeString(handle);
		logger.info("Called getRawHandleValues for handle " + decodedHandle);

		// A decoded handle is something like this
		// http://handle.net/prefix/PIDNumber
		int lastSeparatorIndex = decodedHandle.lastIndexOf('/');

		if (lastSeparatorIndex != -1
				&& (lastSeparatorIndex + 1 < decodedHandle.length())) {

			String pidNumber = decodedHandle.substring(lastSeparatorIndex + 1);
			// String pid = rodaPIDNamespace + ":" + pidNumber;

			HandleValue hValue = getHandleValueFromPIDNumber(pidNumber);

			byte[][] rawValues = new byte[1][];

			rawValues[0] = new byte[Encoder.calcStorageSize(hValue)];
			Encoder.encodeHandleValue(rawValues[0], 0, hValue);

			return rawValues;

		} else {
			logger.error("Handle is not well formed - " + decodedHandle);
			throw new HandleException(HandleException.INVALID_VALUE,
					"Handle is not well formed - " + decodedHandle);
		}

	}

	/**
	 * Returns true if this server is responsible for the given naming
	 * authority.
	 * 
	 * @see HandleStorage#haveNA(byte[])
	 */
	public boolean haveNA(byte[] authHandle) throws HandleException {
		String naHandle = Util.decodeString(authHandle);
		logger.info("Called haveNA for NA " + naHandle);
		return rodaNAHandle.equals(naHandle);
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#checkpointDatabase()
	 */
	public void checkpointDatabase() throws HandleException {
		logger.debug("checkpointDatabase() - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#createHandle(byte[], HandleValue[])
	 */
	public void createHandle(byte[] handle, HandleValue[] values)
			throws HandleException {
		logger.debug("createHandle(...) - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#deleteAllRecords()
	 */
	public void deleteAllRecords() throws HandleException {
		logger.debug("deleteAllRecords() - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#deleteHandle(byte[])
	 */
	public boolean deleteHandle(byte[] handle) throws HandleException {
		logger.debug("deleteHandle(...) - not implemented");
		// true if the handle was in the database, false otherwise.
		return false;
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#scanHandles(ScanCallback)
	 */
	public void scanHandles(ScanCallback callback) throws HandleException {
		logger.debug("scanHandles(...) - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#scanNAs(ScanCallback)
	 */
	public void scanNAs(ScanCallback callback) throws HandleException {
		logger.debug("scanNAs(...) - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#setHaveNA(byte[], boolean)
	 */
	public void setHaveNA(byte[] authHandle, boolean flag)
			throws HandleException {
		logger.debug("setHaveNA(...) - not implemented");
	}

	/**
	 * Not implemented.
	 * 
	 * @see HandleStorage#updateValue(byte[], HandleValue[])
	 */
	public void updateValue(byte[] handle, HandleValue[] value)
			throws HandleException {
		logger.debug("updateValue(...) - not implemented");
	}

	private HandleValue getHandleValueFromPIDNumber(String pidNumber) {
		String url = rodaUrlPrefix + pidNumber;

		HandleValue hValue = new HandleValue();
		hValue.setIndex(100);
		hValue.setType(Util.encodeString("URL"));
		hValue.setData(Util.encodeString(url));
		hValue.setTTLType((byte) 0);
		hValue.setTTL(100);
		hValue.setTimestamp(100);
		hValue.setReferences(null);
		hValue.setAdminCanRead(true);
		hValue.setAdminCanWrite(false);
		hValue.setAnyoneCanRead(true);
		hValue.setAnyoneCanWrite(false);

		return hValue;
	}

	private String getHandleForPID(String doPID) {
		String[] pidParts = doPID.split(":");
		return rodaNAHandle + "/" + pidParts[1];
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

		File externalConfigurationFile = new File(roda_config_directory,
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

}
