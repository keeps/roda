package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.NoSuchReportException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.ReportException;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.reports.ReportManager;
import pt.gov.dgarq.roda.core.reports.ReportManagerException;
import pt.gov.dgarq.roda.core.reports.ReportRegistryException;

/**
 * This class implements Reports service.
 * 
 * @author Rui Castro
 */
public class Reports extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Reports.class);

	private ReportManager reportManager = null;

	/**
	 * Constructs a new {@link Report}.
	 * 
	 * @throws RODAServiceException
	 */
	public Reports() throws RODAServiceException {
		super();

		try {

			reportManager = ReportManager.getDefaultReportManager();

		} catch (ReportManagerException e) {
			logger.debug("Error getting default Report Manager - "
					+ e.getMessage(), e);
			throw new ReportException("Error getting default Report Manager - "
					+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
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
	 * @throws ReportException
	 */
	public Report getReport(String reportID) throws NoSuchReportException,
			ReportException {

		try {

			Date start = new Date();
			Report report = this.reportManager.getReport(reportID);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Reports.getReport", new String[] { "reportID",
					"" + reportID },
					"User %username% called method Reports.getReport("
							+ reportID + ")", duration);

			return report;

		} catch (ReportRegistryException e) {
			logger.debug(e.getMessage(), e);
			throw new ReportException(e.getMessage(), e);
		}

	}

	/**
	 * Returns the number of {@link Report}s that respect the specified filters.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link Report}s.
	 * 
	 * @throws ReportException
	 */
	public int getReportsCount(Filter filter) throws ReportException {

		try {

			Date start = new Date();
			int count = this.reportManager.getReportsCount(filter);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Reports.getReportsCount", new String[] { "filter",
					"" + filter },
					"User %username% called method Reports.getReportsCount("
							+ filter + ")", duration);

			return count;

		} catch (ReportRegistryException e) {
			logger.debug(e.getMessage(), e);
			throw new ReportException(e.getMessage(), e);
		}

	}

	/**
	 * * Returns a list of {@link Report}s matching the {@link ContentAdapter}
	 * specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link Report}.
	 * 
	 * @throws ReportException
	 */
	public Report[] getReports(ContentAdapter contentAdapter)
			throws ReportException {

		try {

			Date start = new Date();
			List<Report> reports = this.reportManager
					.getReports(contentAdapter);
			Report[] result = reports.toArray(new Report[reports.size()]);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Reports.getReports", new String[] {
					"contentAdapter", "" + contentAdapter },
					"User %username% called method Reports.getReports("
							+ contentAdapter + ")", duration);

			return result;

		} catch (ReportRegistryException e) {
			logger.debug(e.getMessage(), e);
			throw new ReportException(e.getMessage(), e);
		}

	}
}
