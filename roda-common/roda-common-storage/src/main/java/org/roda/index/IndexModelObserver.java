package org.roda.index;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.common.RodaConstants;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.File;
import org.roda.model.ModelObserver;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.Representation;
import org.roda.storage.StorageActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class IndexModelObserver implements ModelObserver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SolrClient index;
	private final ModelService model;

	public IndexModelObserver(SolrClient index, ModelService model) {
		super();
		this.index = index;
		this.model = model;
	}

	@Override
	public void aipCreated(final AIP aip) {
		SolrInputDocument aipDoc = SolrUtils.aipToSolrInputDocument(aip);

		try {
			SolrInputDocument sdoDoc = SolrUtils.aipToSolrInputDocumentAsSDO(aip, model);
			index.add(RodaConstants.INDEX_AIP, aipDoc);
			index.commit(RodaConstants.INDEX_AIP);
			logger.debug("Adding SDO: " + sdoDoc);
			index.add(RodaConstants.INDEX_SDO, sdoDoc);
			index.commit(RodaConstants.INDEX_SDO);
		} catch (SolrServerException | IOException | ModelServiceException | StorageActionException
				| IndexActionException e) {
			logger.error("Could not index created AIP", e);
		}

		final List<String> representationIds = aip.getRepresentationIds();
		for (String representationId : representationIds) {
			try {
				Representation representation = model.retrieveRepresentation(aip.getId(), representationId);
				SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
				index.add(RodaConstants.INDEX_REPRESENTATIONS, representationDocument);
			} catch (SolrServerException | IOException | ModelServiceException e) {
				logger.error("Could not index representation", e);
			}
		}
		try {
			index.commit(RodaConstants.INDEX_REPRESENTATIONS);
		} catch (SolrServerException | IOException e) {
			logger.error("Could not commit indexed representations", e);
		}

	}

	@Override
	public void aipUpdated(AIP aip) {
		// TODO Is this the best way to update? What about the commit?
		aipDeleted(aip.getId());
		aipCreated(aip);
	}

	// TODO Handle exceptions
	@Override
	public void aipDeleted(String aipId) {
		try {
			index.deleteById(RodaConstants.INDEX_AIP, aipId);
			index.commit(RodaConstants.INDEX_AIP);

			index.deleteById(RodaConstants.INDEX_SDO, aipId);
			index.commit(RodaConstants.INDEX_SDO);
		} catch (SolrServerException | IOException e) {
			logger.error("Could not index delete AIP", e);
		}

		// TODO delete included representations, descriptive metadata and other
	}

	@Override
	public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
		// re-index whole AIP
		try {
			aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
		} catch (ModelServiceException e) {
			logger.error("Error when descriptive metadata created on retrieving the full AIP", e);
		}
	}

	@Override
	public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
		// re-index whole AIP
		try {
			aipUpdated(model.retrieveAIP(descriptiveMetadata.getAipId()));
		} catch (ModelServiceException e) {
			logger.error("Error when descriptive metadata created on retrieving the full AIP", e);
		}

	}

	@Override
	public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId) {
		// re-index whole AIP
		try {
			aipUpdated(model.retrieveAIP(aipId));
		} catch (ModelServiceException e) {
			logger.error("Error when descriptive metadata created on retrieving the full AIP", e);
		}

	}

	@Override
	public void representationCreated(Representation representation) {

		SolrInputDocument representationDocument = SolrUtils.representationToSolrDocument(representation);
		try {
			index.add(RodaConstants.INDEX_REPRESENTATIONS, representationDocument);
			index.commit(RodaConstants.INDEX_REPRESENTATIONS);
		} catch (SolrServerException | IOException e) {
			logger.error("Could not index created representation", e);
		}

	}

	@Override
	public void representationUpdated(Representation representation) {
		representationDeleted(representation.getAipId(), representation.getId());
		representationCreated(representation);
	}

	@Override
	public void representationDeleted(String aipId, String representationId) {
		try {
			index.deleteById(RodaConstants.INDEX_REPRESENTATIONS, SolrUtils.getId(aipId, representationId));
			index.commit(RodaConstants.INDEX_REPRESENTATIONS);
		} catch (SolrServerException sse) {

		} catch (IOException ioe) {

		}

	}

	@Override
	public void fileCreated(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileUpdated(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileDeleted(String aipId, String representationId, String fileId) {
		// TODO Auto-generated method stub

	}

}
