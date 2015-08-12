package pt.gov.dgarq.roda.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.IngestMonitorException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.ingest.IngestManager;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;

/**
 * This class implements Ingest Monitor service.
 * 
 * @author Rui Castro
 */
public class IngestMonitor extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(IngestMonitor.class);

	private IngestManager ingestManager = null;

	/**
	 * @throws RODAServiceException
	 */
	public IngestMonitor() throws RODAServiceException {
		super();

		try {

			this.ingestManager = IngestManager.getDefaultIngestManager();

		} catch (IngestRegistryException e) {
			throw new IngestMonitorException("Error initializing - "
					+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Returns the number of {@link SIPState}s that respect the specified
	 * filters.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link SIPState}s.
	 * 
	 * @throws IngestMonitorException
	 */
	public int getSIPsCount(Filter filter) throws IngestMonitorException {
		Date start = new Date();

		if (filter == null) {
			filter = new Filter();
		}

		List<FilterParameter> parametersList = new ArrayList<FilterParameter>();

		if (filter.getParameters() != null) {
			parametersList.addAll(filter.getParameters());
		}

		List<String> roles = Arrays.asList(this.getClientUser().getRoles());

		if (!roles.contains("ingest.list_all_sips")) {
			parametersList.add(0, new SimpleFilterParameter("username",
					getClientUser().getName()));
			filter.setParameters(parametersList);
		}

		try {

			int count = this.ingestManager.getSIPsCount(filter);

			long duration = new Date().getTime() - start.getTime();
			registerAction("IngestMonitor.getSIPsCount", new String[] {
					"filter", "" + filter },
					"User %username% called method IngestMonitor.getSIPsCount("
							+ filter + ")", duration);
			return count;

		} catch (IngestRegistryException e) {
			throw new IngestMonitorException(e.getMessage(), e);
		}
	}

	/**
	 * Returns a list of {@link SIPState}s matching the {@link ContentAdapter}
	 * specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link SIPState}.
	 * 
	 * @throws IngestMonitorException
	 */
	public SIPState[] getSIPs(ContentAdapter contentAdapter)
			throws IngestMonitorException {
		Date start = new Date();

		if (contentAdapter == null) {
			contentAdapter = new ContentAdapter();
		}

		Filter contentAdapterFilter = contentAdapter.getFilter();

		if (contentAdapterFilter == null) {
			contentAdapterFilter = new Filter();
			contentAdapter.setFilter(contentAdapterFilter);
		}

		List<FilterParameter> parametersList = new ArrayList<FilterParameter>();

		if (contentAdapterFilter.getParameters() != null) {
			parametersList.addAll(contentAdapterFilter
					.getParameters());
		}

		List<String> roles = Arrays.asList(this.getClientUser().getRoles());

		if (!roles.contains("ingest.list_all_sips")) {
			parametersList.add(0, new SimpleFilterParameter("username",
					getClientUser().getName()));
			contentAdapterFilter.setParameters(parametersList);
		}

		try {

			List<SIPState> sips = this.ingestManager.getSIPs(contentAdapter);
			SIPState[] result = sips.toArray(new SIPState[sips.size()]);

			long duration = new Date().getTime() - start.getTime();
			registerAction("IngestMonitor.getSIPs", new String[] {
					"contentAdapter", contentAdapter + "" },
					"User %username% called method IngestMonitor.getSIPs("
							+ contentAdapter + ")", duration);

			return result;

		} catch (IngestRegistryException e) {
			throw new IngestMonitorException(e.getMessage(), e);
		}
	}

	/**
	 * Returns all the possible states a {@link SIPState} can have.
	 * 
	 * @return an array of {@link String}s with the names of all the states a
	 *         {@link SIPState} can have.
	 */
	public String[] getPossibleStates() {
		Date start = new Date();

		List<String> states = this.ingestManager.getStates();
		states.add(this.ingestManager.getQuarantineState());
		String[] result = states.toArray(new String[states.size()]);

		long duration = new Date().getTime() - start.getTime();
		registerAction(
				"IngestMonitor.getPossibleStates",
				new String[] {},
				"User %username% called method IngestMonitor.getPossibleStates()",
				duration);

		return result;
	}

}
