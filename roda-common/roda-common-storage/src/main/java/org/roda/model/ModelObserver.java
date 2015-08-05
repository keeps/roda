package org.roda.model;

import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SIPState;

public interface ModelObserver {

	public void aipCreated(AIP aip);

	public void aipUpdated(AIP aip);

	public void aipDeleted(String aipId);

	public void descriptiveMetadataCreated(
			DescriptiveMetadata descriptiveMetadataBinary);

	public void descriptiveMetadataUpdated(
			DescriptiveMetadata descriptiveMetadataBinary);

	public void descriptiveMetadataDeleted(String aipId,
			String descriptiveMetadataBinaryId);

	public void representationCreated(Representation representation);

	public void representationUpdated(Representation representation);

	public void representationDeleted(String aipId, String representationId);

	public void fileCreated(File file);

	public void fileUpdated(File file);

	public void fileDeleted(String aipId, String representationId, String fileId);
	
	public void logEntryCreated(LogEntry entry);
	
	public void sipStateCreated(SIPState state);

}
