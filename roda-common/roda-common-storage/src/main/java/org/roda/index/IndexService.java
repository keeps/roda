package org.roda.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.index.filter.Filter;
import org.roda.index.sorter.Sorter;
import org.roda.index.sublist.Sublist;
import org.roda.index.utils.SolrUtils;
import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.Representation;

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

	public String getParent(String id) throws IndexActionException {
		return retrieveAIP(id).getParentId();
	}

	public List<String> getAncestors(String id) throws IndexActionException {
		List<String> ancestors = new ArrayList<>();
		String parentId = null;
		String currId = id;
		do {
			parentId = getParent(currId);
			if (parentId != null) {
				ancestors.add(parentId);
				currId = parentId;
			}
		} while (parentId != null);
		return ancestors;
	}
}
