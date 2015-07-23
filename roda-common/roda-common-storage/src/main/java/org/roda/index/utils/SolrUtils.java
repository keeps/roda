package org.roda.index.utils;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.handler.loader.XMLLoader;
import org.roda.common.RodaUtils;
import org.roda.index.IndexActionException;
import org.roda.index.SimpleEventPreservationMetadata;
import org.roda.index.SimpleRepresentationFileMetadata;
import org.roda.index.SimpleRepresentationPreservationMetadata;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.preservation.EventPreservationObject;
import org.roda.model.preservation.RepresentationFilePreservationObject;
import org.roda.model.preservation.RepresentationPreservationObject;
import org.roda.storage.Binary;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RODAObject;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class SolrUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);

	public static void update(SolrClient solrClient, String collection, ContentStream contentStream, boolean commitNow)
			throws SolrServerException, IOException {
		ContentStreamUpdateRequest request = new ContentStreamUpdateRequest("/update");
		request.addContentStream(contentStream);
		solrClient.request(request, collection);

		if (commitNow) {
			solrClient.commit(collection);
		}
	}

	private static final String ID_SEPARATOR = ".";

	public static String getId(String... ids) {
		StringBuilder ret = new StringBuilder();
		for (String id : ids) {
			if (ret.length() > 0) {
				ret.append(ID_SEPARATOR);
			}
			ret.append(id);
		}

		return ret.toString();
	}

	public static <T extends Serializable> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
			Class<T> responseClass) throws IndexActionException {
		final SolrDocumentList docList = response.getResults();
		final long offset = docList.getStart();
		final long limit = docList.size();
		final long totalCount = docList.getNumFound();
		final List<T> docs = new ArrayList<T>();

		for (SolrDocument doc : docList) {
			T result = solrDocumentTo(responseClass, doc);
			docs.add(result);
		}

		return new IndexResult<>(offset, limit, totalCount, docs);
	}

	public static SolrInputDocument getDescriptiveMetataFields(Binary binary) throws IndexActionException {
		SolrInputDocument doc;
		InputStream inputStream;
		try {
			inputStream = binary.getContent().createInputStream();

			Reader descMetadataReader = new InputStreamReader(inputStream);
			String filename = binary.getStoragePath().getName();

			// TODO select transformers using file name extension
			ClassLoader classLoader = SolrUtils.class.getClassLoader();
			InputStream transformerStream = classLoader.getResourceAsStream(filename + ".xslt");

			if (transformerStream == null) {
				// use default
				transformerStream = classLoader.getResourceAsStream("plain.xslt");
			}

			// TODO support the use of scripts for non-xml transformers

			Reader xsltReader = new InputStreamReader(transformerStream);
			CharArrayWriter transformerResult = new CharArrayWriter();
			Map<String, String> stylesheetOpt = new HashMap<>();
			stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
			RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
			descMetadataReader.close();

			XMLLoader loader = new XMLLoader();
			LOGGER.debug("Transformed desc. metadata:\n" + transformerResult);
			CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

			boolean parsing = true;
			doc = null;
			while (parsing) {
				int event = parser.next();
				switch (event) {
				case XMLStreamConstants.END_DOCUMENT:
					parser.close();
					parsing = false;
					break;
				case XMLStreamConstants.START_ELEMENT:
					String currTag = parser.getLocalName();
					if ("doc".equals(currTag)) {
						doc = loader.readDoc(parser);
					}
					break;
				}

			}
			transformationResult.close();

		} catch (IOException | TransformerException | XMLStreamException | FactoryConfigurationError e) {
			throw new IndexActionException("Could not process descriptive metadata",
					IndexActionException.INTERNAL_SERVER_ERROR, e);
		}
		return doc;
	}

	public static String parseFilter(Filter filter) throws IndexActionException {
		StringBuilder ret = new StringBuilder();

		if (filter == null) {
			ret.append("*:*");
		} else {
			FilterParameter[] parameters = filter.getParameters();
			for (FilterParameter parameter : parameters) {
				if (parameter instanceof SimpleFilterParameter) {
					SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
					if (ret.length() != 0) {
						appendANDOperator(ret);
					}
					appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true);
				} else if (parameter instanceof OneOfManyFilterParameter) {
					OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
					if (ret.length() != 0) {
						appendANDOperator(ret);
					}
					appendValuesUsingOROperator(ret, param.getName(), param.getValues());
				} else if (parameter instanceof BasicSearchFilterParameter) {
					if (ret.length() != 0) {
						appendANDOperator(ret);
					}
					BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
					appendBasicSearch(ret, param.getName(), param.getValue(), "OR");
				} else if (parameter instanceof EmptyKeyFilterParameter) {
					if (ret.length() != 0) {
						appendANDOperator(ret);
					}
					EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
					ret.append("(*:* NOT " + param.getName() + ":*)");
				} else {
					LOGGER.error("Unsupported filter parameter class: " + parameter.getClass().getName());
					throw new IndexActionException(
							"Unsupported filter parameter class: " + parameter.getClass().getName(),
							IndexActionException.BAD_REQUEST);
				}
			}

			if (ret.length() == 0) {
				ret.append("*:*");
			}
		}

		LOGGER.debug("Converting filter {} to query {}", filter, ret);
		return ret.toString();
	}

	private static void appendANDOperator(StringBuilder ret) {
		ret.append(" AND ");
	}

	private static void appendValuesUsingOROperator(StringBuilder ret, String key, String[] values) {
		ret.append("(");
		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				ret.append(" OR ");
			}
			appendExactMatch(ret, key, values[i], true);
		}
		ret.append(")");
	}

	private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes) {
		ret.append("(").append(key).append(": ").append(appendDoubleQuotes ? "\"" : "").append(value)
				.append(appendDoubleQuotes ? "\"" : "").append(")");
	}

	// FIXME escape values for Solr special chars
	private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator) {
		if (value.matches("^\".+$")) {
			appendExactMatch(ret, key, value, false);
		} else {
			String[] split = value.split("\\s+");
			ret.append("(");
			for (int i = 0; i < split.length; i++) {
				if (i != 0 && operator != null) {
					ret.append(" " + operator + " ");
				}
				ret.append(key).append(": ").append(split[i]);
			}
			ret.append(")");
		}
	}

	private static List<String> objectToListString(Object object) {
		List<String> ret;
		if (object == null) {
			ret = null;
		} else if (object instanceof String) {
			List<String> l = new ArrayList<String>();
			l.add((String) object);
			return l;
		} else if (object instanceof List<?>) {
			List<?> l = (List<?>) object;
			ret = new ArrayList<String>();
			for (Object o : l) {
				ret.add(o.toString());
			}
		} else {
			LOGGER.error("Could not convert Solr object to List<String>" + object.getClass().getName());
			ret = null;
		}
		return ret;
	}

	private static Long objectToLong(Object object) {
		Long ret;
		if (object instanceof Long) {
			ret = (Long) object;
		} else if (object instanceof String) {
			try {
				ret = Long.parseLong((String) object);
			} catch (NumberFormatException e) {
				LOGGER.error("Could not convert Solr object to long", e);
				ret = null;
			}
		} else {
			LOGGER.error("Could not convert Solr object to long" + object.getClass().getName());
			ret = null;
		}
		return ret;
	}

	private static Date objectToDate(Object object) {
		Date ret;
		if (object == null) {
			ret = null;
		} else if (object instanceof Date) {
			ret = (Date) object;
		} else if (object instanceof String) {
			try {
				ret = RodaUtils.parseDate((String) object);
			} catch (ParseException e) {
				LOGGER.error("Could not convert Solr object to date", e);
				ret = null;
			}
		} else {
			LOGGER.error("Could not convert Solr object to date, unsupported class: " + object.getClass().getName());
			ret = null;
		}

		return ret;
	}

	private static Boolean objectToBoolean(Object object) {
		Boolean ret;
		if (object instanceof Boolean) {
			ret = (Boolean) object;
		} else if (object instanceof String) {
			ret = Boolean.parseBoolean((String) object);
		} else {
			LOGGER.error("Could not convert Solr object to Boolean" + object.getClass().getName());
			ret = null;
		}
		return ret;
	}

	private static String objectToString(Object object) {
		String ret;
		if (object == null) {
			ret = null;
		} else if (object instanceof String) {
			ret = (String) object;
		} else {
			LOGGER.warn("Could not convert Solr object to string, unsupported class: " + object.getClass().getName());
			ret = object.toString();
		}
		return ret;
	}

	//TODO: Handle SimpleRepresentationPreservationMetadata
	private static <T> String getIndexName(Class<T> resultClass) throws IndexActionException {
		String indexName;
		if (resultClass.equals(AIP.class)) {
			indexName = RodaConstants.INDEX_AIP;
		} else if (resultClass.equals(SimpleDescriptionObject.class)) {
			// FIXME rename INDEX constant
			indexName = RodaConstants.INDEX_SDO;
		} else if (resultClass.equals(Representation.class)) {
			indexName = RodaConstants.INDEX_REPRESENTATIONS;
		} else if(resultClass.equals(SimpleRepresentationFileMetadata.class)) {
			indexName = RodaConstants.INDEX_PRESERVATION_OBJECTS;
		} else if(resultClass.equals(SimpleEventPreservationMetadata.class)){
			indexName = RodaConstants.INDEX_PRESERVATION_EVENTS;
		} else {
			throw new IndexActionException("Cannot find class index name: " + resultClass.getName(),
					IndexActionException.INTERNAL_SERVER_ERROR);
		}
		return indexName;
	}

	private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc) throws IndexActionException {
		T ret;
		if (resultClass.equals(AIP.class)) {
			ret = resultClass.cast(solrDocumentToAIP(doc));
		} else if (resultClass.equals(SimpleDescriptionObject.class)) {
			ret = resultClass.cast(solrDocumentToSDO(doc));
		} else if (resultClass.equals(Representation.class)) {
			ret = resultClass.cast(solrDocumentToRepresentation(doc));
		} else if(resultClass.equals(SimpleRepresentationFileMetadata.class)){
			ret = resultClass.cast(solrDocumentToSimpleRepresentationFileMetadata(doc));
		} else if(resultClass.equals(SimpleEventPreservationMetadata.class)){
			ret = resultClass.cast(solrDocumentToSimpleEventPreservationMetadata(doc));
		} else {
			throw new IndexActionException("Cannot find class index name: " + resultClass.getName(),
					IndexActionException.INTERNAL_SERVER_ERROR);
		}
		return ret;

	}

	public static <T> T retrieve(SolrClient index, Class<T> classToRetrieve, String... ids)
			throws IndexActionException {
		T ret;
		try {
			SolrDocument doc = index.getById(getIndexName(classToRetrieve), SolrUtils.getId(ids));
			if (doc != null) {
				ret = solrDocumentTo(classToRetrieve, doc);
			} else {
				throw new IndexActionException("Document not found", IndexActionException.NOT_FOUND);
			}
		} catch (SolrServerException | IOException e) {
			throw new IndexActionException("Could not retrieve AIP from index",
					IndexActionException.INTERNAL_SERVER_ERROR, e);
		}
		return ret;
	}

	public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve,
			Filter filter, Sorter sorter, Sublist sublist) throws IndexActionException {
		IndexResult<T> ret;
		SolrQuery query = new SolrQuery();
		String queryString = parseFilter(filter);
		query.setQuery(queryString);
		query.setStart(sublist.getFirstElementIndex());
		query.setRows(sublist.getMaximumElementCount());
		try {
			QueryResponse response = index.query(getIndexName(classToRetrieve), query);
			ret = queryResponseToIndexResult(response, classToRetrieve);
		} catch (SolrServerException | IOException e) {
			throw new IndexActionException("Could not query index", IndexActionException.INTERNAL_SERVER_ERROR, e);
		}

		return ret;
	}

	public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
			throws IndexActionException {
		return find(index, classToRetrieve, filter, null, new Sublist(0, 0)).getTotalCount();
	}

	// Handle active
	public static AIP solrDocumentToAIP(SolrDocument doc) {
		final String id = objectToString(doc.get(RodaConstants.AIP_ID));
		final Boolean active = objectToBoolean(doc.get(RodaConstants.AIP_ACTIVE));
		final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID));
		final Date dateCreated = objectToDate(doc.get(RodaConstants.AIP_DATE_CREATED));
		final Date dateModified = objectToDate(doc.get(RodaConstants.AIP_DATE_MODIFIED));
		final List<String> descriptiveMetadataFileIds = objectToListString(
				doc.get(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID));
		final List<String> representationIds = objectToListString(doc.get(RodaConstants.AIP_REPRESENTATION_ID));
		/*
		final List<String> preservationObjectsIds = objectToListString(doc.get(RodaConstants.AIP_PRESERVATION_OBJECTS_ID));
		final List<String> preservationEventsIds = objectToListString(doc.get(RodaConstants.AIP_PRESERVATION_EVENTS_ID));
		*/
		return new AIP(id, parentId, active, dateCreated, dateModified, descriptiveMetadataFileIds, representationIds,null,null,null);
	}

	public static SolrInputDocument aipToSolrInputDocument(AIP aip) {
		SolrInputDocument ret = new SolrInputDocument();

		ret.addField(RodaConstants.AIP_ID, aip.getId());
		ret.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
		ret.addField(RodaConstants.AIP_ACTIVE, aip.isActive());
		ret.addField(RodaConstants.AIP_DATE_CREATED, aip.getDateCreated());
		ret.addField(RodaConstants.AIP_DATE_MODIFIED, aip.getDateModified());
		ret.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, aip.getDescriptiveMetadataIds());
		ret.addField(RodaConstants.AIP_REPRESENTATION_ID, aip.getRepresentationIds());

		return ret;
	}

	// FIXME rename this
	public static SimpleDescriptionObject solrDocumentToSDO(SolrDocument doc) {
		final String id = objectToString(doc.get(RodaConstants.AIP_ID));
		final String label = id;
		final Boolean active = objectToBoolean(doc.get(RodaConstants.AIP_ACTIVE));
		final String state = active ? RODAObject.STATE_ACTIVE : RODAObject.STATE_INACTIVE;
		final Date dateCreated = objectToDate(doc.get(RodaConstants.AIP_DATE_CREATED));
		final Date dateModified = objectToDate(doc.get(RodaConstants.AIP_DATE_MODIFIED));
		final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID));

		final List<String> levels = objectToListString(doc.get(RodaConstants.SDO_LEVEL));
		final List<String> titles = objectToListString(doc.get(RodaConstants.SDO_TITLE));
		final List<String> descriptions = objectToListString(doc.get(RodaConstants.SDO_DESCRIPTION));
		final Date dateInitial = objectToDate(doc.get(RodaConstants.SDO_DATE_INITIAL));
		final Date dateFinal = objectToDate(doc.get(RodaConstants.SDO_DATE_FINAL));

		final String level = levels != null ? levels.get(0) : null;
		final String title = titles != null ? titles.get(0) : null;
		final String description = descriptions != null ? descriptions.get(0) : null;
		final int childrenCount = 0;

		return new SimpleDescriptionObject(id, label, dateModified, dateCreated, state, level, title, dateInitial,
				dateFinal, description, parentId, childrenCount);

	}

	public static SolrInputDocument aipToSolrInputDocumentAsSDO(AIP aip, ModelService model)
			throws ModelServiceException, StorageActionException, IndexActionException {
		final SolrInputDocument ret = new SolrInputDocument();
		final String aipId = aip.getId();

		ret.addField(RodaConstants.AIP_ID, aipId);
		ret.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
		ret.addField(RodaConstants.AIP_ACTIVE, aip.isActive());
		ret.addField(RodaConstants.AIP_DATE_CREATED, aip.getDateCreated());
		ret.addField(RodaConstants.AIP_DATE_MODIFIED, aip.getDateModified());

		for (String descId : aip.getDescriptiveMetadataIds()) {
			DescriptiveMetadata metadata = model.retrieveDescriptiveMetadata(aipId, descId);
			StoragePath storagePath = metadata.getStoragePath();
			Binary binary = model.getStorage().getBinary(storagePath);
			SolrInputDocument fields = getDescriptiveMetataFields(binary);

			for (SolrInputField field : fields) {
				ret.addField(field.getName(), field.getValue(), field.getBoost());
			}
		}

		// TODO add information for SDO
		// TODO add sub-documents with full descriptive metadata info

		return ret;
	}

	public static Representation solrDocumentToRepresentation(SolrDocument doc) {
		final String id = objectToString(doc.get(RodaConstants.SRO_ID));
		final String aipId = objectToString(RodaConstants.SRO_AIP_ID);
		final Boolean active = objectToBoolean(RodaConstants.SRO_ACTIVE);
		final Date dateCreated = objectToDate(doc.get(RodaConstants.SRO_DATE_CREATION));
		final Date dateModified = objectToDate(doc.get(RodaConstants.SRO_DATE_MODIFICATION));
		final Set<RepresentationState> statuses = new HashSet<RepresentationState>();
		Collection<Object> fieldValues = doc.getFieldValues(RodaConstants.SRO_STATUS);
		for (Object statusField : fieldValues) {
			String statusAsString = objectToString(statusField);
			if (statusAsString != null) {
				RepresentationState state = RepresentationState.valueOf(statusAsString);
				statuses.add(state);
			} else {
				LOGGER.error("Error parsing representation status, found a NULL");
			}
		}
		final List<String> fileIds = objectToListString(doc.get(RodaConstants.SRO_FILE_IDS));

		final String type = objectToString(doc.get(RodaConstants.SRO_TYPE));
		return new Representation(id, aipId, active, dateCreated, dateModified, statuses, type, fileIds);
	}

	public static SolrInputDocument representationToSolrDocument(Representation rep) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(RodaConstants.SRO_UUID, getId(rep.getAipId(), rep.getId()));
		doc.addField(RodaConstants.SRO_ID, rep.getId());
		doc.addField(RodaConstants.SRO_AIP_ID, rep.getAipId());
		doc.addField(RodaConstants.SRO_DATE_CREATION, rep.getDateCreated());
		doc.addField(RodaConstants.SRO_DATE_MODIFICATION, rep.getDateModified());
		if (rep.getStatuses() != null && rep.getStatuses().size() > 0) {
			for (RepresentationState rs : rep.getStatuses()) {
				doc.addField(RodaConstants.SRO_STATUS, rs.toString());
			}
		}
		doc.addField(RodaConstants.SRO_TYPE, rep.getType());

		doc.addField(RodaConstants.SRO_FILE_IDS, rep.getFileIds());
		return doc;
	}

	
	public static SimpleEventPreservationMetadata solrDocumentToSimpleEventPreservationMetadata(SolrDocument doc) {
		final String  agentID = objectToString(doc.get(RodaConstants.SEPM_AGENT_ID));
		final Date dateCreated = objectToDate(doc.get(RodaConstants.SEPM_CREATED_DATE));
		final String id = objectToString(doc.get(RodaConstants.SEPM_ID));
		final String label = objectToString(doc.get(RodaConstants.SEPM_LABEL));
		final Date modifiedDate = objectToDate(doc.get(RodaConstants.SEPM_LAST_MODIFIED_DATE));
		final String state = objectToString(doc.get(RodaConstants.SEPM_STATE));
		final String targetID = objectToString(doc.get(RodaConstants.SEPM_TARGET_ID));
		final String type = objectToString(doc.get(RodaConstants.SEPM_TYPE));
		final String aipID = objectToString(doc.get(RodaConstants.SEPM_AIP_ID));
		final String representationID = objectToString(doc.get(RodaConstants.SEPM_REPRESENTATION_ID));
		final String fileID = objectToString(doc.get(RodaConstants.SEPM_FILE_ID));
		final Date date = objectToDate(doc.get(RodaConstants.SEPM_DATETIME));
		final String name = objectToString(doc.get(RodaConstants.SEPM_NAME));
		final String description = objectToString(doc.get(RodaConstants.SEPM_DESCRIPTION));
		final String outcomeResult = objectToString(doc.get(RodaConstants.SEPM_OUTCOME_RESULT));
		final String outcomeDetails = objectToString(doc.get(RodaConstants.SEPM_OUTCOME_DETAILS));
		
		SimpleEventPreservationMetadata sepm = new SimpleEventPreservationMetadata();
		sepm.setAgentID(agentID);
		sepm.setAipId(aipID);
		sepm.setRepresentationId(representationID);
		sepm.setFileId(fileID);
		sepm.setCreatedDate(dateCreated);
		sepm.setDate(date);
		sepm.setDescription(description);
		sepm.setLabel(label);
		sepm.setLastModifiedDate(modifiedDate);
		sepm.setName(name);
		sepm.setOutcomeDetails(outcomeDetails);
		sepm.setOutcomeResult(outcomeResult);
		sepm.setState(state);
		sepm.setTargetID(targetID);
		sepm.setType(type);
		return sepm;
	}
	public static SimpleRepresentationPreservationMetadata solrDocumentToSimpleRepresentationPreservationMetadata(SolrDocument doc) {
		final Date dateCreated = objectToDate(doc.get(RodaConstants.SRPM_CREATED_DATE));
		final String id = objectToString(doc.get(RodaConstants.SRPM_ID));
		final String label = objectToString(doc.get(RodaConstants.SRPM_LABEL));
		final Date modifiedDate = objectToDate(doc.get(RodaConstants.SRPM_LAST_MODIFIED_DATE));
		final String representationObjectId = objectToString(doc.get(RodaConstants.SRPM_REPRESENTATION_OBJECT_ID));
		final String type = objectToString(doc.get(RodaConstants.SRPM_TYPE));
		final String state = objectToString(doc.get(RodaConstants.SRPM_STATE));
		final String model = objectToString(doc.get(RodaConstants.SRPM_MODEL));
		final String aipID = objectToString(doc.get(RodaConstants.SRPM_AIP_ID));
		final String representationID = objectToString(doc.get(RodaConstants.SRPM_REPRESENTATION_ID));
		final String fileID = objectToString(doc.get(RodaConstants.SRPM_FILE_ID));
		
		
		SimpleRepresentationPreservationMetadata srpm = new SimpleRepresentationPreservationMetadata();
		srpm.setCreatedDate(dateCreated);
		srpm.setId(id);
		srpm.setID(id); //TODO ???
		srpm.setLabel(label);
		srpm.setLastModifiedDate(modifiedDate);
		srpm.setModel(model);
		srpm.setRepresentationObjectID(representationObjectId);
		srpm.setState(state);
		srpm.setType(type);
		srpm.setAipId(aipID);
		srpm.setRepresentationId(representationID);
		srpm.setFileId(fileID);
		
		return srpm;
	}
	public static SimpleRepresentationFileMetadata solrDocumentToSimpleRepresentationFileMetadata(SolrDocument doc) {
		final Date dateCreated = objectToDate(doc.get(RodaConstants.SRFM_CREATED_DATE));
		final String id = objectToString(doc.get(RodaConstants.SRFM_ID));
		final String label = objectToString(doc.get(RodaConstants.SRFM_LABEL));
		final Date modifiedDate = objectToDate(doc.get(RodaConstants.SRFM_LAST_MODIFIED_DATE));
		final String representationObjectId = objectToString(doc.get(RodaConstants.SRFM_REPRESENTATION_OBJECT_ID));
		final String type = objectToString(doc.get(RodaConstants.SRFM_TYPE));
		final String state = objectToString(doc.get(RodaConstants.SRFM_STATE));
		final String hash = objectToString(doc.get(RodaConstants.SRFM_HASH));
		final String mimetype = objectToString(doc.get(RodaConstants.SRFM_MIMETYPE));
		final String pronomID = objectToString(doc.get(RodaConstants.SRFM_PRONOM_ID));
		final long size = objectToLong(doc.get(RodaConstants.SRFM_SIZE));
		final String aipID = objectToString(doc.get(RodaConstants.SRFM_AIP_ID));
		final String representationID = objectToString(doc.get(RodaConstants.SRFM_REPRESENTATION_ID));
		final String fileID = objectToString(doc.get(RodaConstants.SRFM_FILE_ID));
		SimpleRepresentationFileMetadata srpm = new SimpleRepresentationFileMetadata();
		srpm.setCreatedDate(dateCreated);
		srpm.setFileId(id);
		srpm.setHash(hash);
		srpm.setId(id);
		srpm.setID(id); //TODO: ??????????
		srpm.setLabel(label);
		srpm.setLastModifiedDate(modifiedDate);
		srpm.setMimetype(mimetype);
		srpm.setPronomId(pronomID);
		srpm.setRepresentationObjectId(representationObjectId);
		srpm.setSize(size);
		srpm.setState(state);
		srpm.setType(type);
		srpm.setAipId(aipID);
		srpm.setRepresentationId(representationID);
		srpm.setFileId(fileID);
		return srpm;
	}

	public static SolrInputDocument representationPreservationObjectToSolrDocument(
		String id, RepresentationPreservationObject premisObject) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(RodaConstants.SRPM_CREATED_DATE, premisObject.getCreatedDate());
		doc.addField(RodaConstants.SRPM_ID, id);
		doc.addField(RodaConstants.SRPM_LABEL, premisObject.getLabel());
		doc.addField(RodaConstants.SRPM_LAST_MODIFIED_DATE, premisObject.getLastModifiedDate());
		doc.addField(RodaConstants.SRPM_REPRESENTATION_OBJECT_ID, premisObject.getRepresentationObjectID());
		doc.addField(RodaConstants.SRPM_STATE, premisObject.getState());
		doc.addField(RodaConstants.SRPM_TYPE, premisObject.getType());
		doc.addField(RodaConstants.SRPM_MODEL, premisObject.getModel());
		doc.addField(RodaConstants.SRPM_AIP_ID, premisObject.getAipId());
		doc.addField(RodaConstants.SRPM_REPRESENTATION_ID, premisObject.getRepresentationId());
		doc.addField(RodaConstants.SRPM_FILE_ID, premisObject.getFileId());
		return doc;
	}

	public static SolrInputDocument eventPreservationObjectToSolrDocument(
			String id, EventPreservationObject premisEvent) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(RodaConstants.SEPM_AGENT_ID, premisEvent.getAgentID());
		doc.addField(RodaConstants.SEPM_CREATED_DATE, premisEvent.getCreatedDate());
		doc.addField(RodaConstants.SEPM_ID, id);
		doc.addField(RodaConstants.SEPM_LABEL, premisEvent.getLabel());
		doc.addField(RodaConstants.SEPM_LAST_MODIFIED_DATE, premisEvent.getLastModifiedDate());
		doc.addField(RodaConstants.SEPM_STATE, premisEvent.getState());
		doc.addField(RodaConstants.SEPM_TARGET_ID, premisEvent.getTargetID());
		doc.addField(RodaConstants.SEPM_TYPE, premisEvent.getType());
		doc.addField(RodaConstants.SEPM_DATETIME, premisEvent.getDatetime());
		doc.addField(RodaConstants.SEPM_NAME, premisEvent.getLabel());
		doc.addField(RodaConstants.SEPM_DESCRIPTION, premisEvent.getEventDetail());
		doc.addField(RodaConstants.SEPM_OUTCOME_RESULT, premisEvent.getOutcome());
		doc.addField(RodaConstants.SEPM_OUTCOME_DETAILS, premisEvent.getOutcomeDetailNote());
		doc.addField(RodaConstants.SEPM_AIP_ID, premisEvent.getAipId());
		doc.addField(RodaConstants.SEPM_REPRESENTATION_ID, premisEvent.getRepresentationId());
		doc.addField(RodaConstants.SEPM_FILE_ID, premisEvent.getFileId());
		return doc;
	}

	public static SolrInputDocument representationFilePreservationObjectToSolrDocument(
			String id,RepresentationFilePreservationObject representationFile) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(RodaConstants.SRFM_CREATED_DATE, representationFile.getCreatedDate());
		doc.addField(RodaConstants.SRFM_HASH, representationFile.getHash());
		doc.addField(RodaConstants.SRFM_ID, id);
		doc.addField(RodaConstants.SRFM_LABEL, representationFile.getLabel());
		doc.addField(RodaConstants.SRFM_LAST_MODIFIED_DATE, representationFile.getLastModifiedDate());
		doc.addField(RodaConstants.SRFM_MIMETYPE, representationFile.getFormatDesignationName());
		doc.addField(RodaConstants.SRFM_PRONOM_ID, representationFile.getPronomId());
		doc.addField(RodaConstants.SRFM_REPRESENTATION_OBJECT_ID, representationFile.getRepresentationObjectId());
		doc.addField(RodaConstants.SRFM_SIZE, representationFile.getSize());
		doc.addField(RodaConstants.SRFM_STATE, representationFile.getState());
		doc.addField(RodaConstants.SRFM_TYPE, representationFile.getType());
		doc.addField(RodaConstants.SRFM_AIP_ID, representationFile.getAipId());
		doc.addField(RodaConstants.SRFM_REPRESENTATION_ID, representationFile.getRepresentationId());
		doc.addField(RodaConstants.SRFM_FILE_ID, representationFile.getFileId());
		return doc;
	}

}
