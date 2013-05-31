package pt.gov.dgarq.roda.core.reports;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.NoSuchReportException;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

/**
 * This class implements the RODA Report Manager.
 * 
 * @author Rui Castro
 */
public class ReportManager {

	static final private Logger logger = Logger.getLogger(ReportManager.class);

	static private ReportManager defaultReportManager = null;

	/**
	 * Returns the default {@link ReportManager}. If it doesn't exist, a new
	 * {@link ReportManager} it will be created and returned.
	 * 
	 * @return a {@link ReportManager}.
	 * 
	 * @throws ReportManagerException
	 *             if the {@link ReportManager} couldn't be created.
	 */
	public static ReportManager getDefaultReportManager()
			throws ReportManagerException {
		if (defaultReportManager == null) {
			defaultReportManager = new ReportManager();
		}
		return defaultReportManager;
	}

	private ReportDatabaseUtility databaseUtility = null;

	/**
	 * Creates a new {@link ReportManager}.
	 * 
	 * @throws ReportManagerException
	 */
	private ReportManager() throws ReportManagerException {

		try {

			Configuration configuration = RodaWebApplication.getConfiguration(
					getClass(), "reports.properties");

			String jdbcDriver = configuration.getString("jdbcDriver");
			String jdbcURL = configuration.getString("jdbcURL");
			String jdbcUsername = configuration.getString("jdbcUsername");
			String jdbcPassword = configuration.getString("jdbcPassword");

			if (databaseUtility == null) {
				databaseUtility = new ReportDatabaseUtility(jdbcDriver,
						jdbcURL, jdbcUsername, jdbcPassword);
			}

		} catch (ConfigurationException e) {
			logger.debug(
					"Error reading configuration file - " + e.getMessage(), e);
			throw new ReportManagerException(
					"Error reading configuration file - " + e.getMessage(), e);
		} catch (ReportRegistryException e) {
			logger.debug("Error creating ReportDatabaseUtility - "
					+ e.getMessage(), e);
			throw new ReportManagerException(
					"Error creating ReportDatabaseUtility - " + e.getMessage(),
					e);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Inserts a new {@link Report} into the database.
	 * 
	 * @param report
	 *            the {@link Report} to insert.
	 * 
	 * 
	 * @return the inserted {@link Report}.
	 * 
	 * @throws ReportRegistryException
	 *             if a database exception occurred.
	 */
	public Report insertReport(Report report) throws ReportRegistryException {
		if (report == null) {
			throw new ReportRegistryException("argument report cannot be null");
		} else {
			return this.databaseUtility.insertReport(report);
		}
	}

	/**
	 * Returns the {@link Report} with the specified ID.
	 * 
	 * @param reportID
	 *            the ID of the {@link Report}.
	 * 
	 * @return a {@link Report}
	 * 
	 * @throws NoSuchReportException
	 * @throws ReportRegistryException
	 */
	public Report getReport(String reportID) throws NoSuchReportException,
			ReportRegistryException {
		return this.databaseUtility.getReport(reportID);
	}

	/**
	 * Returns the number of {@link Report}s that respect the specified filters.
	 * 
	 * @param contentAdapterFilter
	 * 
	 * @return the number of {@link Report}s.
	 * 
	 * @throws ReportRegistryException
	 */
	public int getReportsCount(Filter contentAdapterFilter)
			throws ReportRegistryException {
		return this.databaseUtility.getReportsCount(contentAdapterFilter);
	}

	/**
	 * * Returns a list of {@link Report}s matching the {@link ContentAdapter}
	 * specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return a {@link List} of {@link Report}.
	 * 
	 * @throws ReportRegistryException
	 */
	public List<Report> getReports(ContentAdapter contentAdapter)
			throws ReportRegistryException {

		return this.databaseUtility.getReports(contentAdapter);
	}

}
