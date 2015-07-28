package org.roda.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.ModelService;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleEventPreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationFilePreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationPreservationMetadata;

public class IndexService {

	private final SolrClient index;
	private final ModelService model;

	public IndexService(SolrClient index, ModelService model) {
		super();
		this.index = index;
		this.model = model;

		IndexModelObserver observer = new IndexModelObserver(index, model);
		model.addModelObserver(observer);
	}

	public AIP retrieveAIP(String aipId) throws IndexActionException {
		return SolrUtils.retrieve(index, AIP.class, aipId);
	}

	public IndexResult<AIP> findAIP(Filter filter, Sorter sorter, Sublist sublist) throws IndexActionException {
		return SolrUtils.find(index, AIP.class, filter, sorter, sublist);
	}

	public Long countAIP(Filter filter) throws IndexActionException {
		return SolrUtils.count(index, AIP.class, filter);
	}

	public IndexResult<SimpleDescriptionObject> findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist)
			throws IndexActionException {
		return SolrUtils.find(index, SimpleDescriptionObject.class, filter, sorter, sublist);
	}

	public Long countDescriptiveMetadata(Filter filter) throws IndexActionException {
		return SolrUtils.count(index, SimpleDescriptionObject.class, filter);
	}

	public SimpleDescriptionObject retrieveDescriptiveMetadata(String aipId) throws IndexActionException {
		// TODO check if return type shouldn't be updated to
		// SimpleDescriptiveMetadata
		return SolrUtils.retrieve(index, SimpleDescriptionObject.class, aipId);
	}

	public Representation retrieveRepresentation(String aipId, String repId) throws IndexActionException {
		return SolrUtils.retrieve(index, Representation.class, aipId, repId);
	}

	public IndexResult<Representation> findRepresentation(Filter filter, Sorter sorter, Sublist sublist)
			throws IndexActionException {
		return SolrUtils.find(index, Representation.class, filter, sorter, sublist);
	}

	public Long countRepresentations(Filter filter) throws IndexActionException {
		return SolrUtils.count(index, Representation.class, filter);
	}

	public String getParentId(String id) throws IndexActionException {
		return retrieveAIP(id).getParentId();
	}

	public SimpleDescriptionObject getParent(String id) throws IndexActionException {
		return SolrUtils.retrieve(index, SimpleDescriptionObject.class, getParentId(id));
	}

	public SimpleDescriptionObject getParent(SimpleDescriptionObject sdo) throws IndexActionException {
		return SolrUtils.retrieve(index, SimpleDescriptionObject.class, sdo.getParentID());
	}

	public List<SimpleDescriptionObject> getAncestors(SimpleDescriptionObject sdo) throws IndexActionException {
		List<SimpleDescriptionObject> ancestors = new ArrayList<SimpleDescriptionObject>();
		SimpleDescriptionObject parent = null, actual = sdo;

		while (actual != null && actual.getParentID() != null) {
			parent = getParent(actual);
			if (parent != null) {
				ancestors.add(parent);
				actual = parent;
			}
		}

		return ancestors;
	}

	public List<SimpleDescriptionObject> getAncestors(String id) throws IndexActionException {
		List<SimpleDescriptionObject> ancestors = new ArrayList<SimpleDescriptionObject>();
		SimpleDescriptionObject parent = null;
		String currId = id;

		do {
			parent = getParent(currId);
			if (parent != null) {
				ancestors.add(parent);
				currId = parent.getId();
			}
		} while (parent != null && parent.getParentID() != null);

		return ancestors;
	}

	// TODO:
	public SimpleRepresentationPreservationMetadata retrieveSimpleRepresentationPreservationMetadata(String aipId,
			String representationId, String fileId) throws IndexActionException {
		return SolrUtils.retrieve(index, SimpleRepresentationPreservationMetadata.class, aipId, representationId,
				fileId);
	}

	// public Long countSimpleRepresentationPreservationMetadata(Filter filter)
	// throws IndexActionException {
	// return SolrUtils.count(index,
	// SimpleRepresentationPreservationMetadata.class, filter);
	// }
	// public IndexResult<SimpleRepresentationPreservationMetadata>
	// findRepresentationPreservationMetadata(Filter filter, Sorter sorter,
	// Sublist sublist)
	// throws IndexActionException {
	// return SolrUtils.find(index,
	// SimpleRepresentationPreservationMetadata.class, filter, sorter, sublist);
	// }
	// /*
	// public IndexResult<SimpleRepresentationPreservationObject>
	// findRepresentationPreservationObject(ContentAdapter contentAdapter){
	// return null;
	// }
	// */
	//
	public SimpleEventPreservationMetadata retrieveSimpleEventPreservationMetadata(String aipId,
			String representationId, String fileId) throws IndexActionException {
		return SolrUtils.retrieve(index, SimpleEventPreservationMetadata.class, aipId, representationId, fileId);
	}

	public Long countSimpleEventPreservationMetadata(Filter filter) throws IndexActionException {
		return SolrUtils.count(index, SimpleEventPreservationMetadata.class, filter);
	}

	public IndexResult<SimpleEventPreservationMetadata> findSimpleEventPreservationMetadata(Filter filter,
			Sorter sorter, Sublist sublist) throws IndexActionException {
		return SolrUtils.find(index, SimpleEventPreservationMetadata.class, filter, sorter, sublist);
	}

	public SimpleRepresentationFilePreservationMetadata retrieveSimpleRepresentationFilePreservationMetadata(String aipId,
			String representationId, String fileId) throws IndexActionException {
		return SolrUtils.retrieve(index, SimpleRepresentationFilePreservationMetadata.class, aipId, representationId, fileId);
	}

	public Long countSimpleRepresentationFilePreservationMetadata(Filter filter) throws IndexActionException {
		return SolrUtils.count(index, SimpleRepresentationFilePreservationMetadata.class, filter);
	}

	public IndexResult<SimpleRepresentationFilePreservationMetadata> findSimpleRepresentationFilePreservationMetadata(Filter filter,
			Sorter sorter, Sublist sublist) throws IndexActionException {
		return SolrUtils.find(index, SimpleRepresentationFilePreservationMetadata.class, filter, sorter, sublist);
	}

	/*
	 * public IndexResult<SimpleRepresentationPreservationObject>
	 * findRepresentationPreservationObject(ContentAdapter contentAdapter){
	 * return null; }
	 */
	
	//LOG
	public Long getLogEntriesCount(Filter filter) throws IndexActionException{
		return SolrUtils.count(index, LogEntry.class, filter);
	}

	public IndexResult<LogEntry> findLogEntry(Filter filter,
			Sorter sorter, Sublist sublist) throws IndexActionException {
		return SolrUtils.find(index, LogEntry.class, filter, sorter, sublist);
	}
}
