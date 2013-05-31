package pt.gov.dgarq.roda.plugins.maintenance.ingest;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.plugins.Plugin;

/**
 * @author Rui Castro
 */
public class RejectIngestedSIPsTaskPlugin extends AbstractRejectSIPTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(RejectIngestedSIPsTaskPlugin.class);

	private final String name = "Ingest Maintenance/Reject 'SIP_INGESTED' SIPs"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Rejects SIPs in state 'SIP_INGESTED' that match the specified parameters."; //$NON-NLS-1$

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public RejectIngestedSIPsTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
		super();
	}

	/**
	 * @return a {@link String} with the name of this plugin.
	 * @see Plugin#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a float with the version number of this plugin.
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * 
	 * @return a {@link String} with the description of this plugin.
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return description;
	}

}
