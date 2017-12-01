/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.handler.loader.XMLLoader;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.dips.DIPUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DateGranularity;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetParameter.SORT;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.FiltersParameters;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.HasPermissionFilters;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent.PreservationMetadataEventClass;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Utilities class related to Apache Solr
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luís Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public class SolrUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);

  private static final String DEFAULT_QUERY_PARSER_OPERATOR = "AND";

  private static final Set<String> NON_REPEATABLE_FIELDS = new HashSet<>(Arrays.asList(RodaConstants.AIP_TITLE,
    RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));

  private static Map<String, List<String>> liteFieldsForEachClass = new HashMap<>();

  private SolrUtils() {
    // do nothing
  }

  /*
   * Search & Retrieval
   * ____________________________________________________________________________________________________________________
   */

  public static <T extends IsIndexed> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
    throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0), new ArrayList<>()).getTotalCount();
  }

  public static <T extends IsIndexed> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter, User user,
    boolean justActive) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0), null, user, justActive, new ArrayList<>())
      .getTotalCount();
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, Class<T> classToRetrieve, String id,
    List<String> fieldsToReturn) throws NotFoundException, GenericException {
    if (id == null) {
      throw new GenericException("Could not retrieve object from a null id");
    }

    T ret;
    try {
      SolrDocument doc = index.getById(getIndexName(classToRetrieve).get(0), id);
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc, fieldsToReturn);
      } else {
        throw new NotFoundException("Could not find document " + id);
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve object from index", e);
    }
    return ret;
  }

  public static <T extends IsIndexed> List<T> retrieve(SolrClient index, Class<T> classToRetrieve, List<String> id,
    List<String> fieldsToReturn) throws NotFoundException, GenericException {
    List<T> ret = new ArrayList<>();
    try {
      int block = RodaConstants.DEFAULT_PAGINATION_VALUE;
      for (int i = 0; i < id.size(); i += block) {
        List<String> subList = id.subList(i, (i + block <= id.size() ? i + block : id.size()));
        SolrDocumentList docs = index.getById(getIndexName(classToRetrieve).get(0), subList);
        for (SolrDocument doc : docs) {
          ret.add(solrDocumentTo(classToRetrieve, doc, fieldsToReturn));
        }
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve object from index", e);
    }
    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, sorter, sublist, null, fieldsToReturn);
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
    }
    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve).get(0), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets, fieldsToReturn);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static <T extends IsIndexed> List<String> getClassLiteFields(Class<T> classToRetrieve) {
    List<String> ret;
    if (liteFieldsForEachClass.containsKey(classToRetrieve.getName())) {
      ret = liteFieldsForEachClass.get(classToRetrieve.getName());
    } else {
      try {
        ret = classToRetrieve.newInstance().liteFields();
        liteFieldsForEachClass.put(classToRetrieve.getName(), ret);
      } catch (InstantiationException | IllegalAccessException e) {
        LOGGER.error("Error instantiating object of type {}", classToRetrieve.getName(), e);
        ret = new ArrayList<>();
      }
    }
    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, User user, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    query.setFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
    parseAndConfigureFacets(facets, query);
    if (hasPermissionFilters(classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, justActive, classToRetrieve));
    }

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve).get(0), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets, fieldsToReturn);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e.getMessage());
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */

  private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc, List<String> fieldsToReturn)
    throws GenericException {
    T ret;
    if (resultClass.equals(IndexedAIP.class)) {
      ret = resultClass.cast(solrDocumentToIndexedAIP(doc, fieldsToReturn));
    } else if (resultClass.equals(IndexedRepresentation.class) || resultClass.equals(Representation.class)) {
      ret = resultClass.cast(solrDocumentToRepresentation(doc, fieldsToReturn));
    } else if (resultClass.equals(LogEntry.class)) {
      ret = resultClass.cast(solrDocumentToLogEntry(doc, fieldsToReturn));
    } else if (resultClass.equals(Report.class) || resultClass.equals(IndexedReport.class)) {
      ret = resultClass.cast(solrDocumentToJobReport(doc, fieldsToReturn));
    } else if (resultClass.equals(RODAMember.class) || resultClass.equals(User.class)
      || resultClass.equals(Group.class)) {
      ret = resultClass.cast(solrDocumentToRodaMember(doc));
    } else if (resultClass.equals(TransferredResource.class)) {
      ret = resultClass.cast(solrDocumentToTransferredResource(doc));
    } else if (resultClass.equals(Job.class)) {
      ret = resultClass.cast(solrDocumentToJob(doc, fieldsToReturn));
    } else if (resultClass.equals(Risk.class) || resultClass.equals(IndexedRisk.class)) {
      ret = resultClass.cast(solrDocumentToRisk(doc));
    } else if (resultClass.equals(RepresentationInformation.class)) {
      ret = resultClass.cast(solrDocumentToRepresentationInformation(doc));
    } else if (resultClass.equals(Notification.class)) {
      ret = resultClass.cast(solrDocumentToNotification(doc, fieldsToReturn));
    } else if (resultClass.equals(RiskIncidence.class)) {
      ret = resultClass.cast(solrDocumentToRiskIncidence(doc));
    } else if (resultClass.equals(DIP.class) || resultClass.equals(IndexedDIP.class)) {
      ret = resultClass.cast(solrDocumentToIndexedDIP(doc, fieldsToReturn));
    } else if (resultClass.equals(DIPFile.class)) {
      ret = resultClass.cast(solrDocumentToDIPFile(doc));
    } else if (resultClass.equals(IndexedFile.class)) {
      ret = resultClass.cast(solrDocumentToIndexedFile(doc, fieldsToReturn));
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      ret = resultClass.cast(solrDocumentToIndexedPreservationEvent(doc));
    } else if (resultClass.equals(IndexedPreservationAgent.class)) {
      ret = resultClass.cast(solrDocumentToIndexedPreservationAgent(doc));
    } else if (resultClass.equals(Format.class)) {
      ret = resultClass.cast(solrDocumentToFormat(doc));
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return ret;
  }

  private static <T> SolrInputDocument toSolrDocument(Class<T> resultClass, T object)
    throws GenericException, NotSupportedException {

    SolrInputDocument ret;
    if (resultClass.equals(IndexedAIP.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(IndexedRepresentation.class) || resultClass.equals(Representation.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(LogEntry.class)) {
      ret = logEntryToSolrDocument((LogEntry) object);
    } else if (resultClass.equals(Report.class) || resultClass.equals(IndexedReport.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(RODAMember.class) || resultClass.equals(User.class)
      || resultClass.equals(Group.class)) {
      ret = rodaMemberToSolrDocument((RODAMember) object);
    } else if (resultClass.equals(TransferredResource.class)) {
      ret = transferredResourceToSolrDocument((TransferredResource) object);
    } else if (resultClass.equals(Job.class)) {
      ret = jobToSolrDocument((Job) object);
    } else if (resultClass.equals(Risk.class) || resultClass.equals(IndexedRisk.class)) {
      ret = riskToSolrDocument((Risk) object, 0);
    } else if (resultClass.equals(RepresentationInformation.class)) {
      ret = representationInformationToSolrDocument((RepresentationInformation) object);
    } else if (resultClass.equals(Notification.class)) {
      ret = notificationToSolrDocument((Notification) object);
    } else if (resultClass.equals(RiskIncidence.class)) {
      ret = riskIncidenceToSolrDocument((RiskIncidence) object);
    } else if (resultClass.equals(DIP.class) || resultClass.equals(IndexedDIP.class)) {
      ret = dipToSolrDocument((DIP) object);
    } else if (resultClass.equals(IndexedFile.class) || resultClass.equals(DIPFile.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(IndexedPreservationAgent.class)) {
      throw new NotSupportedException();
    } else if (resultClass.equals(Format.class)) {
      ret = formatToSolrDocument((Format) object);
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return ret;
  }

  public static <T extends Serializable> List<String> getIndexName(Class<T> resultClass) throws GenericException {
    List<String> indexNames = new ArrayList<>();

    // INFO 201608 nvieira: the first index name must be the "main" one
    if (resultClass.equals(AIP.class) || resultClass.equals(IndexedAIP.class)) {
      indexNames.add(RodaConstants.INDEX_AIP);
      indexNames.add(RodaConstants.INDEX_REPRESENTATION);
      indexNames.add(RodaConstants.INDEX_FILE);
      // INFO nvieira 20170207 events should be cleared on specific plugin
    } else if (resultClass.equals(Representation.class) || resultClass.equals(IndexedRepresentation.class)) {
      indexNames.add(RodaConstants.INDEX_REPRESENTATION);
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      indexNames.add(RodaConstants.INDEX_PRESERVATION_EVENTS);
    } else if (resultClass.equals(IndexedPreservationAgent.class)) {
      indexNames.add(RodaConstants.INDEX_PRESERVATION_AGENTS);
    } else if (resultClass.equals(LogEntry.class)) {
      indexNames.add(RodaConstants.INDEX_ACTION_LOG);
    } else if (resultClass.equals(Report.class) || resultClass.equals(IndexedReport.class)) {
      indexNames.add(RodaConstants.INDEX_JOB_REPORT);
    } else if (resultClass.equals(User.class) || resultClass.equals(Group.class)
      || resultClass.equals(RODAMember.class)) {
      indexNames.add(RodaConstants.INDEX_MEMBERS);
    } else if (resultClass.equals(TransferredResource.class)) {
      indexNames.add(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
    } else if (resultClass.equals(Job.class)) {
      indexNames.add(RodaConstants.INDEX_JOB);
      indexNames.add(RodaConstants.INDEX_JOB_REPORT);
    } else if (resultClass.equals(IndexedFile.class) || resultClass.equals(File.class)) {
      indexNames.add(RodaConstants.INDEX_FILE);
    } else if (resultClass.equals(Risk.class) || resultClass.equals(IndexedRisk.class)) {
      indexNames.add(RodaConstants.INDEX_RISK);
    } else if (resultClass.equals(RepresentationInformation.class)) {
      indexNames.add(RodaConstants.INDEX_REPRESENTATION_INFORMATION);
    } else if (resultClass.equals(Notification.class)) {
      indexNames.add(RodaConstants.INDEX_NOTIFICATION);
    } else if (resultClass.equals(RiskIncidence.class)) {
      indexNames.add(RodaConstants.INDEX_RISK_INCIDENCE);
    } else if (resultClass.equals(DIP.class) || resultClass.equals(IndexedDIP.class)) {
      indexNames.add(RodaConstants.INDEX_DIP);
      indexNames.add(RodaConstants.INDEX_DIP_FILE);
    } else if (resultClass.equals(DIPFile.class)) {
      indexNames.add(RodaConstants.INDEX_DIP_FILE);
    } else if (resultClass.equals(Format.class)) {
      indexNames.add(RodaConstants.INDEX_FORMAT);
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }

    return indexNames;
  }

  private static <T> boolean hasPermissionFilters(Class<T> resultClass) throws GenericException {
    return HasPermissionFilters.class.isAssignableFrom(resultClass);
  }

  /**
   * Method that knows how to escape characters for Solr
   * <p>
   * <code>+ - && || ! ( ) { } [ ] ^ " ~ * ? : /</code>
   * </p>
   * <p>
   * Note: chars <code>'*'</code> are not being escaped on purpose
   * </p>
   * 
   * @return a string with special characters escaped
   */
  // FIXME perhaps && and || are not being properly escaped: see how to do it
  public static String escapeSolrSpecialChars(String string) {
    return string.replaceAll("([+&|!(){}\\[\\-\\]\\^\\\\~?:\"/])", "\\\\$1");
  }

  private static List<String> objectToListString(Object object) {
    List<String> ret;
    if (object == null) {
      ret = new ArrayList<>();
    } else if (object instanceof String) {
      List<String> l = new ArrayList<>();
      l.add((String) object);
      return l;
    } else if (object instanceof List<?>) {
      List<?> l = (List<?>) object;
      ret = new ArrayList<>();
      for (Object o : l) {
        ret.add(o.toString());
      }
    } else {
      LOGGER.error("Could not convert Solr object to List<String> ({})", object.getClass().getName());
      ret = new ArrayList<>();
    }
    return ret;
  }

  private static List<RepresentationInformationRelation> objectToListRelation(Object object) {
    List<RepresentationInformationRelation> ret;
    if (object == null) {
      ret = new ArrayList<>();
    } else if (object instanceof String) {
      try {
        List<LinkedHashMap<String, String>> l = JsonUtils.getObjectFromJson((String) object, List.class);
        ret = new ArrayList<>();
        for (LinkedHashMap<String, String> entry : l) {
          ret.add(
            new RepresentationInformationRelation(entry.get(RodaConstants.REPRESENTATION_INFORMATION_RELATION_TYPE),
              RelationObjectType.valueOf(entry.get(RodaConstants.REPRESENTATION_INFORMATION_OBJECT_TYPE)),
              entry.get(RodaConstants.REPRESENTATION_INFORMATION_LINK),
              entry.get(RodaConstants.REPRESENTATION_INFORMATION_TITLE)));
        }
      } catch (GenericException e) {
        LOGGER.error("Could not convert Solr object to List<RepresentationInformationRelation> ({})",
          object.getClass().getName(), e);
        ret = new ArrayList<>();
      }
    } else {
      LOGGER.error("Could not convert Solr object to List<RepresentationInformationRelation> ({})",
        object.getClass().getName());
      ret = new ArrayList<>();
    }
    return ret;
  }

  public static Integer objectToInteger(Object object, Integer defaultValue) {
    Integer ret = defaultValue;
    if (object != null) {
      if (object instanceof Integer) {
        ret = (Integer) object;
      } else if (object instanceof String) {
        try {
          ret = Integer.parseInt((String) object);
        } catch (NumberFormatException e) {
          LOGGER.error("Could not convert Solr object to integer", e);
        }
      } else {
        LOGGER.error("Could not convert Solr object to integer ({})", object.getClass().getName());
      }
    }

    return ret;
  }

  public static Long objectToLong(Object object, Long defaultValue) {
    Long ret = defaultValue;
    if (object != null) {
      if (object instanceof Long) {
        ret = (Long) object;
      } else if (object instanceof String) {
        try {
          ret = Long.parseLong((String) object);
        } catch (NumberFormatException e) {
          LOGGER.error("Could not convert Solr object to long", e);
        }
      } else {
        LOGGER.error("Could not convert Solr object to long ({})", object.getClass().getName());
      }
    }
    return ret;
  }

  @SuppressWarnings("unused")
  private static Float objectToFloat(Object object) {
    Float ret;
    if (object instanceof Float) {
      ret = (Float) object;
    } else if (object instanceof String) {
      try {
        ret = Float.parseFloat((String) object);
      } catch (NumberFormatException e) {
        LOGGER.error("Could not convert Solr object to float", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to float ({})", object.getClass().getName());
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
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = RodaUtils.parseDate((String) object);
      } catch (ParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  private static Boolean objectToBoolean(Object object, Boolean defaultValue) {
    Boolean ret = defaultValue;
    if (object != null) {
      if (object instanceof Boolean) {
        ret = (Boolean) object;
      } else if (object instanceof String) {
        ret = Boolean.parseBoolean((String) object);
      } else {
        LOGGER.error("Could not convert Solr object to Boolean ({})", object.getClass().getName());
      }
    }
    return ret;
  }

  private static String objectToString(Object object, String defaultValue) {
    String ret = defaultValue;
    if (object != null) {
      if (object instanceof String) {
        ret = (String) object;
      } else {
        LOGGER.warn("Could not convert Solr object to string, unsupported class: {}", object.getClass().getName());
      }
    }
    return ret;
  }

  private static <E extends Enum<E>> E objectToEnum(Object object, Class<E> enumeration, E defaultValue) {
    E ret = defaultValue;
    if (object != null) {
      if (object instanceof String) {
        String name = (String) object;
        try {
          ret = Enum.valueOf(enumeration, name);
        } catch (IllegalArgumentException e) {
          LOGGER.warn("Invalid name for enumeration: {}, name: {}", enumeration.getName(), name);
        } catch (NullPointerException e) {
          LOGGER.warn("Error parsing enumeration: {}, name: {}", enumeration.getName(), name);
        }
      } else {
        LOGGER.warn("Could not convert Solr object to enumeration: {}, unsupported class: {}", enumeration.getName(),
          object.getClass().getName());
      }
    }
    return ret;
  }

  /**
   * @deprecated use {@link #objectToString(Object, String)} instead
   */
  @Deprecated
  private static String objectToString(Object object) {
    String ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof String) {
      ret = (String) object;
    } else {
      LOGGER.warn("Could not convert Solr object to string, unsupported class: {}", object.getClass().getName());
      ret = object.toString();
    }
    return ret;
  }

  private static <T extends Serializable> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    Class<T> responseClass, Facets facets, List<String> liteFields) throws GenericException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<>();

    for (SolrDocument doc : docList) {
      T result = solrDocumentTo(responseClass, doc, liteFields);
      docs.add(result);
    }

    return new IndexResult<>(offset, limit, totalCount, docs, facetResults);
  }

  private static List<FacetFieldResult> processFacetFields(Facets facets, List<FacetField> facetFields) {
    List<FacetFieldResult> ret = new ArrayList<>();
    FacetFieldResult facetResult;
    if (facetFields != null) {
      for (FacetField facet : facetFields) {
        LOGGER.trace("facet:{} count:{}", facet.getName(), facet.getValueCount());
        facetResult = new FacetFieldResult(facet.getName(), facet.getValueCount(),
          facets.getParameters().get(facet.getName()).getValues());
        for (Count count : facet.getValues()) {
          LOGGER.trace("   value:{} value:{}", count.getName(), count.getCount());
          facetResult.addFacetValue(count.getName(), count.getName(), count.getCount());
        }
        ret.add(facetResult);
      }
    }
    return ret;

  }

  public static SolrInputDocument getDescriptiveMetadataFields(Binary binary, String metadataType,
    String metadataVersion) throws GenericException {
    SolrInputDocument doc;

    Map<String, String> parameters = new HashMap<>();
    parameters.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
    Reader transformationResult = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_CROSSWALKS_INGEST,
      metadataType, metadataVersion, parameters);

    try {
      XMLLoader loader = new XMLLoader();
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

      boolean parsing = true;
      doc = null;
      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT) {
          String currTag = parser.getLocalName();
          if ("doc".equals(currTag)) {
            doc = loader.readDoc(parser);
          }
        }
      }

    } catch (XMLStreamException | FactoryConfigurationError e) {
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath(), e);
    } finally {
      IOUtils.closeQuietly(transformationResult);
    }

    return doc == null ? new SolrInputDocument() : validateDescriptiveMetadataFields(doc);
  }

  private static SolrInputDocument validateDescriptiveMetadataFields(SolrInputDocument doc) {
    if (doc.get(RodaConstants.AIP_DATE_INITIAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_INITIAL).getValue();
      if (value instanceof String) {
        try {
          Date d = DateUtil.parseDate((String) value);
          doc.setField(RodaConstants.AIP_DATE_INITIAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_INITIAL);
          doc.setField(RodaConstants.AIP_DATE_INITIAL + "_txt", value);
        }
      }
    }

    if (doc.get(RodaConstants.AIP_DATE_FINAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_FINAL).getValue();
      if (value instanceof String) {
        try {
          Date d = DateUtil.parseDate((String) value);
          doc.setField(RodaConstants.AIP_DATE_FINAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_FINAL);
          doc.setField(RodaConstants.AIP_DATE_FINAL + "_txt", value);
        }
      }
    }

    return doc;
  }

  /*
   * Roda Filter > Apache Solr query
   * ____________________________________________________________________________________________________________________
   */

  public static String parseFilter(Filter filter) throws RequestNotValidException {
    StringBuilder ret = new StringBuilder();

    if (filter == null || filter.getParameters().isEmpty()) {
      ret.append("*:*");
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        parseFilterParameter(ret, parameter, true);
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.trace("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
      appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
      appendValuesUsingOROperator(ret, param.getName(), param.getValues(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
      appendANDOperator(ret, true);
      ret.append("(*:* NOT " + param.getName() + ":*)");
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
        processToDate(param.getToValue(), param.getGranularity(), false), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof DateIntervalFilterParameter) {
      DateIntervalFilterParameter param = (DateIntervalFilterParameter) parameter;
      appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
        param.getGranularity(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter param = (LongRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof NotSimpleFilterParameter) {
      NotSimpleFilterParameter notSimplePar = (NotSimpleFilterParameter) parameter;
      appendNotExactMatch(ret, notSimplePar.getName(), notSimplePar.getValue(), true,
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OrFiltersParameters || parameter instanceof AndFiltersParameters) {
      FiltersParameters filters = (FiltersParameters) parameter;
      appendFiltersWithOperator(ret, parameter instanceof OrFiltersParameters ? "OR" : "AND", filters.getValues(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      LOGGER.error("Unsupported filter parameter class: {}", parameter.getClass().getName());
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
    }
  }

  private static void appendANDOperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" AND ");
    }
  }

  private static void appendOROperator(StringBuilder ret, boolean prefixWithOROperatorIfBuilderNotEmpty) {
    if (prefixWithOROperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" OR ");
    }
  }

  private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    ret.append("(").append(key).append(": ");
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(value.replaceAll("(\")", "\\\\$1"));
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(")");
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (!values.isEmpty()) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (StringUtils.isBlank(value)) {
      appendExactMatch(ret, key, "*", false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (value.matches("^\".+\"$")) {
      appendExactMatch(ret, key, value.substring(1, value.length() - 1), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      appendWhiteSpaceTokenizedString(ret, key, value, operator, prefixWithANDOperatorIfBuilderNotEmpty);
    }
  }

  private static void appendKeyValue(StringBuilder ret, String key, String value) {
    ret.append(key).append(":").append("(").append(value).append(")");
  }

  private static void appendFiltersWithOperator(StringBuilder ret, String operator, List<FilterParameter> values,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    if (!values.isEmpty()) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" ").append(operator).append(" ");
        }
        parseFilterParameter(ret, values.get(i), false);
      }
      ret.append(")");
    }
  }

  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

    String[] split = value.trim().split("\\s+");
    ret.append("(");
    for (int i = 0; i < split.length; i++) {
      if (i != 0 && operator != null) {
        ret.append(" " + operator + " ");
      }
      if (split[i].matches("(AND|OR|NOT)")) {
        ret.append(key).append(": \"").append(split[i]).append("\"");
      } else {
        ret.append(key).append(": (").append(escapeSolrSpecialChars(split[i])).append(")");
      }
    }
    ret.append(")");
  }

  private static <T extends Serializable, T1 extends Serializable> void appendRange(StringBuilder ret, String key,
    Class<T> fromClass, T fromValue, Class<T1> toClass, T1 toValue, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(").append(key).append(":[");
      generateRangeValue(ret, fromClass, fromValue);
      ret.append(" TO ");
      generateRangeValue(ret, toClass, toValue);
      ret.append("])");
    }
  }

  private static <T extends Serializable> void generateRangeValue(StringBuilder ret, Class<T> valueClass, T value) {
    if (value != null) {
      if (valueClass.equals(Date.class)) {
        String date = DateUtil.getThreadLocalDateFormat().format(Date.class.cast(value));
        LOGGER.trace("Appending date value \"{}\" to range", date);
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append(Long.class.cast(value));
      } else if (valueClass.equals(String.class)) {
        ret.append(String.class.cast(value));
      } else {
        LOGGER.error("Cannot process range of the type {}", valueClass);
      }
    } else {
      ret.append("*");
    }
  }

  private static void appendRangeInterval(StringBuilder ret, String fromKey, String toKey, Date fromValue, Date toValue,
    DateGranularity granularity, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
      ret.append("(");

      ret.append(fromKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]").append(" OR ");

      ret.append(toKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]");

      if (fromValue != null && toValue != null) {
        ret.append(" OR ").append("(").append(fromKey).append(":[* TO ").append(processToDate(fromValue, granularity))
          .append("]");
        ret.append(" AND ").append(toKey).append(":[").append(processFromDate(toValue)).append(" TO *]").append(")");
      }

      ret.append(")");
    }
  }

  private static String processFromDate(Date fromValue) {
    final String ret;

    if (fromValue != null) {
      return DateUtil.getThreadLocalDateFormat().format(fromValue);
    } else {
      ret = "*";
    }

    return ret;
  }

  private static String processToDate(Date toValue, DateGranularity granularity) {
    return processToDate(toValue, granularity, true);
  }

  private static String processToDate(Date toValue, DateGranularity granularity, boolean returnAsteriskOnNull) {
    final String ret;
    StringBuilder sb = new StringBuilder();
    if (toValue != null) {
      sb.append(DateUtil.getThreadLocalDateFormat().format(toValue));
      switch (granularity) {
        case YEAR:
          sb.append("+1YEAR-1MILLISECOND");
          break;
        case MONTH:
          sb.append("+1MONTH-1MILLISECOND");
          break;
        case DAY:
          sb.append("+1DAY-1MILLISECOND");
          break;
        case HOUR:
          sb.append("+1HOUR-1MILLISECOND");
          break;
        case MINUTE:
          sb.append("+1MINUTE-1MILLISECOND");
          break;
        case SECOND:
          sb.append("+1SECOND-1MILLISECOND");
          break;
        default:
          // do nothing
          break;
      }
      ret = sb.toString();
    } else {
      ret = returnAsteriskOnNull ? "*" : null;
    }
    return ret;
  }

  private static void appendNotExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendExactMatch(ret, "*:* -" + key, value, appendDoubleQuotes, prefixWithANDOperatorIfBuilderNotEmpty);
  }

  public static String getLastScanDate(Date scanDate) {
    return DateUtil.getThreadLocalDateFormat().format(scanDate);
  }

  /*
   * Roda Sorter > Apache Solr Sort clauses
   * ____________________________________________________________________________________________________________________
   */
  public static List<SortClause> parseSorter(Sorter sorter) {
    List<SortClause> ret = new ArrayList<>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SortClause(sortParameter.getName(), sortParameter.isDescending() ? ORDER.desc : ORDER.asc));
      }
    }
    return ret;
  }

  /*
   * Roda Facets > Apache Solr Facets
   * ____________________________________________________________________________________________________________________
   */
  private static void parseAndConfigureFacets(Facets facets, SolrQuery query) {
    if (facets != null) {
      query.setFacetSort(getSolrFacetParameterSortValue(FacetParameter.DEFAULT_SORT));
      if (!"".equals(facets.getQuery())) {
        query.addFacetQuery(facets.getQuery());
      }
      StringBuilder filterQuery = new StringBuilder();
      for (Entry<String, FacetParameter> parameter : facets.getParameters().entrySet()) {
        FacetParameter facetParameter = parameter.getValue();
        setSolrFacetParameterSort(query, facetParameter);

        if (facetParameter instanceof SimpleFacetParameter) {
          setQueryFacetParameter(query, (SimpleFacetParameter) facetParameter);
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(),
            ((SimpleFacetParameter) facetParameter).getValues(), true);
        } else {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        }
      }
      if (filterQuery.length() > 0) {
        query.addFilterQuery(filterQuery.toString());
        LOGGER.trace("Query after defining facets: {}", query);
      }
    }
  }

  private static void setSolrFacetParameterSort(SolrQuery query, FacetParameter facetParameter) {
    if (FacetParameter.DEFAULT_SORT != facetParameter.getSort()) {
      query.add(String.format("f.%s.facet.sort", facetParameter.getName()),
        getSolrFacetParameterSortValue(facetParameter.getSort()));
    }
  }

  private static String getSolrFacetParameterSortValue(SORT facetSort) {
    return facetSort == SORT.INDEX ? FacetParams.FACET_SORT_INDEX : FacetParams.FACET_SORT_COUNT;
  }

  private static void setQueryFacetParameter(SolrQuery query, SimpleFacetParameter facetParameter) {
    query.addFacetField(facetParameter.getName());

    query.add(String.format("f.%s.facet.mincount", facetParameter.getName()),
      String.valueOf(facetParameter.getMinCount()));
    query.add(String.format("f.%s.facet.limit", facetParameter.getName()), String.valueOf(facetParameter.getLimit()));

  }

  /*
   * Roda user > Apache Solr filter query
   * ____________________________________________________________________________________________________________________
   */
  private static <T extends IsIndexed> String getFilterQueries(User user, boolean justActive,
    Class<T> classToRetrieve) {

    StringBuilder fq = new StringBuilder();

    // TODO find a better way to define admin super powers
    if (user != null && !RodaConstants.ADMIN.equals(user.getName())) {
      fq.append("(");
      String usersKey = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + PermissionType.READ;
      appendExactMatch(fq, usersKey, user.getId(), true, false);

      String groupsKey = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + PermissionType.READ;
      appendValuesUsingOROperatorForQuery(fq, groupsKey, new ArrayList<>(user.getGroups()), true);

      fq.append(")");
    }

    if (justActive && !IndexedDIP.class.equals(classToRetrieve) && !DIPFile.class.equals(classToRetrieve)) {
      appendExactMatch(fq, RodaConstants.STATE, AIPState.ACTIVE.toString(), true, true);
    }

    return fq.toString();
  }

  private static void appendValuesUsingOROperatorForQuery(StringBuilder ret, String key, List<String> values,
    boolean prependWithOrIfNeeded) {
    if (!values.isEmpty()) {
      if (prependWithOrIfNeeded) {
        appendOROperator(ret, true);
      } else {
        appendANDOperator(ret, true);
      }

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  /*
   * Apache Solr helper methods
   * ____________________________________________________________________________________________________________________
   */
  private static void commit(SolrClient index, String... collections) {

    boolean waitFlush = false;
    boolean waitSearcher = true;
    boolean softCommit = true;

    for (String collection : collections) {
      try {
        index.commit(collection, waitFlush, waitSearcher, softCommit);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error commiting into collection: {}", collection, e);
      }
    }
  }

  public static void commit(SolrClient index, List<Class<? extends IsIndexed>> resultClasses) throws GenericException {
    List<String> collections = new ArrayList<>();
    for (Class<? extends IsIndexed> resultClass : resultClasses) {
      collections.add(getIndexName(resultClass).get(0));
    }

    commit(index, collections.toArray(new String[] {}));
  }

  @SafeVarargs
  public static void commit(SolrClient index, Class<? extends IsIndexed>... resultClasses) throws GenericException {
    commit(index, Arrays.asList(resultClasses));
  }

  public static <T extends IsIndexed> void create(SolrClient index, Class<T> classToCreate, T instance)
    throws GenericException {
    try {
      index.add(getIndexName(classToCreate).get(0), toSolrDocument(classToCreate, instance));
    } catch (SolrServerException | IOException | NotSupportedException e) {
      throw new GenericException("Error adding instance to index", e);
    }
  }

  /*
   * Crosswalks: RODA Objects <-> Apache Solr documents
   * ____________________________________________________________________________________________________________________
   */

  public static IndexedAIP solrDocumentToIndexedAIP(SolrDocument doc, List<String> fieldsToReturn) {
    final String id = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    AIPState state = null;

    if (doc.containsKey(RodaConstants.STATE)) {
      state = AIPState.valueOf(objectToString(doc.get(RodaConstants.STATE), AIPState.getDefault().toString()));
    }

    final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID), null);
    final List<String> ingestSIPIds = objectToListString(doc.get(RodaConstants.INGEST_SIP_IDS));
    final String ingestJobId = objectToString(doc.get(RodaConstants.INGEST_JOB_ID), "");
    final List<String> ingestUpdateJobIds = objectToListString(doc.get(RodaConstants.INGEST_UPDATE_JOB_IDS));
    final List<String> allIngestJobIds = objectToListString(doc.get(RodaConstants.ALL_INGEST_JOB_IDS));
    final List<String> ancestors = objectToListString(doc.get(RodaConstants.AIP_ANCESTORS));
    final String type = objectToString(doc.get(RodaConstants.AIP_TYPE), "");
    final List<String> levels = objectToListString(doc.get(RodaConstants.AIP_LEVEL));
    final List<String> titles = objectToListString(doc.get(RodaConstants.AIP_TITLE));
    final List<String> descriptions = objectToListString(doc.get(RodaConstants.AIP_DESCRIPTION));
    final Date dateInitial = objectToDate(doc.get(RodaConstants.AIP_DATE_INITIAL));
    final Date dateFinal = objectToDate(doc.get(RodaConstants.AIP_DATE_FINAL));
    final Long numberOfSubmissionFiles = objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_SUBMISSION_FILES), 0L);
    final Long numberOfDocumentationFiles = objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_DOCUMENTATION_FILES), 0L);
    final Long numberOfSchemaFiles = objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_SCHEMA_FILES), 0L);

    final Boolean hasRepresentations = objectToBoolean(doc.get(RodaConstants.AIP_HAS_REPRESENTATIONS), Boolean.FALSE);
    final Boolean ghost = objectToBoolean(doc.get(RodaConstants.AIP_GHOST), Boolean.FALSE);

    Permissions permissions = getPermissions(doc);
    final String title = titles.isEmpty() ? null : titles.get(0);
    final String description = descriptions.isEmpty() ? null : descriptions.get(0);

    final Date createdOn = objectToDate(doc.get(RodaConstants.AIP_CREATED_ON));
    final String createdBy = objectToString(doc.get(RodaConstants.AIP_CREATED_BY), "");
    final Date updatedOn = objectToDate(doc.get(RodaConstants.AIP_UPDATED_ON));
    final String updatedBy = objectToString(doc.get(RodaConstants.AIP_UPDATED_BY), "");

    String level;
    if (ghost) {
      level = RodaConstants.AIP_GHOST;
    } else {
      level = levels.isEmpty() ? null : levels.get(0);
    }

    Map<String, Object> indexedFields = new HashMap<>();
    for (String field : fieldsToReturn) {
      indexedFields.put(field, doc.get(field));
    }

    return new IndexedAIP(id, state, type, level, title, dateInitial, dateFinal, description, parentId, ancestors,
      permissions, numberOfSubmissionFiles, numberOfDocumentationFiles, numberOfSchemaFiles, hasRepresentations, ghost)
        .setIngestSIPIds(ingestSIPIds).setIngestJobId(ingestJobId).setIngestUpdateJobIds(ingestUpdateJobIds)
        .setCreatedOn(createdOn).setCreatedBy(createdBy).setUpdatedOn(updatedOn).setUpdatedBy(updatedBy)
        .setAllUpdateJobIds(allIngestJobIds).setFields(indexedFields);
  }

  public static SolrInputDocument aipToSolrInputDocument(AIP aip, List<String> ancestors, ModelService model,
    boolean safemode)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, aip.getId());
    doc.addField(RodaConstants.AIP_ID, aip.getId());
    doc.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
    doc.addField(RodaConstants.STATE, aip.getState().toString());
    doc.addField(RodaConstants.AIP_TYPE, aip.getType());

    doc.addField(RodaConstants.AIP_CREATED_ON, aip.getCreatedOn());
    doc.addField(RodaConstants.AIP_CREATED_BY, aip.getCreatedBy());
    doc.addField(RodaConstants.AIP_UPDATED_ON, aip.getUpdatedOn());
    doc.addField(RodaConstants.AIP_UPDATED_BY, aip.getUpdatedBy());

    doc.addField(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
    doc.addField(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
    doc.addField(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());

    ArrayList<String> allJobIds = new ArrayList<>(aip.getIngestUpdateJobIds());
    allJobIds.add(aip.getIngestJobId());
    doc.addField(RodaConstants.ALL_INGEST_JOB_IDS, allJobIds);

    doc.addField(RodaConstants.AIP_ANCESTORS, ancestors);

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());

    doc.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, descriptiveMetadataIds);

    List<String> representationIds = aip.getRepresentations().stream().map(r -> r.getId()).collect(Collectors.toList());
    doc.addField(RodaConstants.AIP_REPRESENTATION_ID, representationIds);
    doc.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, !representationIds.isEmpty());

    setPermissions(aip.getPermissions(), doc);

    doc.addField(RodaConstants.AIP_GHOST, aip.getGhost() != null ? aip.getGhost() : false);

    if (!safemode) {
      // guarding against repeated fields
      Set<String> usedNonRepeatableFields = new HashSet<>();

      for (DescriptiveMetadata metadata : aip.getDescriptiveMetadata()) {
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aip.getId(), metadata.getId());
        Binary binary = model.getStorage().getBinary(storagePath);
        try {
          SolrInputDocument fields = getDescriptiveMetadataFields(binary, metadata.getType(), metadata.getVersion());
          for (SolrInputField field : fields) {
            if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
              boolean added = usedNonRepeatableFields.add(field.getName());
              if (added) {
                doc.addField(field.getName(), field.getValue(), field.getBoost());
              }
            } else {
              doc.addField(field.getName(), field.getValue(), field.getBoost());
            }
          }
        } catch (GenericException e) {
          LOGGER.info("Problem processing descriptive metadata: {}", e.getMessage(), e);
        } catch (Exception e) {
          LOGGER.error("Error processing descriptive metadata: {}", metadata, e);
        }
      }
    }

    // Calculate number of documentation and schema files
    StorageService storage = model.getStorage();

    Long numberOfSubmissionFiles;
    try {
      Directory submissionDirectory = model.getSubmissionDirectory(aip.getId());
      numberOfSubmissionFiles = storage.countResourcesUnderDirectory(submissionDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfSubmissionFiles = 0L;
    }

    Long numberOfDocumentationFiles;
    try {
      Directory documentationDirectory = model.getDocumentationDirectory(aip.getId());
      numberOfDocumentationFiles = storage.countResourcesUnderDirectory(documentationDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfDocumentationFiles = 0L;
    }

    Long numberOfSchemaFiles;
    try {
      Directory schemasDirectory = model.getSchemasDirectory(aip.getId());
      numberOfSchemaFiles = storage.countResourcesUnderDirectory(schemasDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfSchemaFiles = 0L;
    }

    doc.addField(RodaConstants.AIP_NUMBER_OF_SUBMISSION_FILES, numberOfSubmissionFiles);
    doc.addField(RodaConstants.AIP_NUMBER_OF_DOCUMENTATION_FILES, numberOfDocumentationFiles);
    doc.addField(RodaConstants.AIP_NUMBER_OF_SCHEMA_FILES, numberOfSchemaFiles);

    return doc;
  }

  public static IndexedRepresentation solrDocumentToRepresentation(SolrDocument doc, List<String> fieldsToReturn) {
    final String uuid = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    final String id = objectToString(doc.get(RodaConstants.REPRESENTATION_ID), null);
    final String aipId = objectToString(doc.get(RodaConstants.REPRESENTATION_AIP_ID), null);
    final Boolean original = objectToBoolean(doc.get(RodaConstants.REPRESENTATION_ORIGINAL), Boolean.FALSE);
    final String type = objectToString(doc.get(RodaConstants.REPRESENTATION_TYPE), null);

    final Long sizeInBytes = objectToLong(doc.get(RodaConstants.REPRESENTATION_SIZE_IN_BYTES), 0L);
    final Long totalNumberOfFiles = objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES), 0L);

    final Long numberOfDocumentationFiles = objectToLong(
      doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES), 0L);
    final Long numberOfSchemaFiles = objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES), 0L);

    final List<String> ancestors = objectToListString(doc.get(RodaConstants.AIP_ANCESTORS));

    IndexedRepresentation rep = new IndexedRepresentation(uuid, id, aipId, Boolean.TRUE.equals(original), type,
      sizeInBytes, totalNumberOfFiles, numberOfDocumentationFiles, numberOfSchemaFiles, ancestors);
    rep.setCreatedOn(objectToDate(doc.get(RodaConstants.REPRESENTATION_CREATED_ON)));
    rep.setCreatedBy(objectToString(doc.get(RodaConstants.REPRESENTATION_CREATED_BY), ""));
    rep.setUpdatedOn(objectToDate(doc.get(RodaConstants.REPRESENTATION_UPDATED_ON)));
    rep.setUpdatedBy(objectToString(doc.get(RodaConstants.REPRESENTATION_UPDATED_BY), ""));

    rep.setRepresentationStates(objectToListString(doc.get(RodaConstants.REPRESENTATION_STATES)));

    Map<String, Object> indexedFields = new HashMap<>();
    for (String field : fieldsToReturn) {
      indexedFields.put(field, doc.get(field));
    }

    rep.setFields(indexedFields);
    return rep;
  }

  public static SolrInputDocument representationToSolrDocument(AIP aip, Representation rep, Long sizeInBytes,
    Long numberOfDataFiles, Long numberOfDocumentationFiles, Long numberOfSchemaFiles, List<String> ancestors, ModelService model, boolean safemode)
          throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException
  {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, IdUtils.getRepresentationId(rep));
    doc.addField(RodaConstants.REPRESENTATION_ID, rep.getId());
    doc.addField(RodaConstants.REPRESENTATION_AIP_ID, rep.getAipId());
    doc.addField(RodaConstants.REPRESENTATION_ORIGINAL, rep.isOriginal());
    doc.addField(RodaConstants.REPRESENTATION_TYPE, rep.getType());

    doc.addField(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, sizeInBytes);
    doc.addField(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, numberOfDataFiles);
    doc.addField(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES, numberOfDocumentationFiles);
    doc.addField(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES, numberOfSchemaFiles);

    doc.addField(RodaConstants.REPRESENTATION_CREATED_ON, rep.getCreatedOn());
    doc.addField(RodaConstants.REPRESENTATION_CREATED_BY, rep.getCreatedBy());
    doc.addField(RodaConstants.REPRESENTATION_UPDATED_ON, rep.getUpdatedOn());
    doc.addField(RodaConstants.REPRESENTATION_UPDATED_BY, rep.getUpdatedBy());

    if (!rep.getRepresentationStates().isEmpty()) {
      doc.addField(RodaConstants.REPRESENTATION_STATES, rep.getRepresentationStates());
    } else if (rep.isOriginal()) {
      doc.addField(RodaConstants.REPRESENTATION_STATES, Arrays.asList(RepresentationState.ORIGINAL));
    }

    // indexing active state and permissions
    doc.addField(RodaConstants.STATE, aip.getState().toString());
    doc.addField(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
    doc.addField(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
    doc.addField(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());
    doc.addField(RodaConstants.REPRESENTATION_ANCESTORS, ancestors);

    setPermissions(aip.getPermissions(), doc);

    // pataki@: taken from aipToSolrInputDocument. TODO: may make refactor this to a separate method
    if (!safemode) {
      // guarding against repeated fields
      Set<String> usedNonRepeatableFields = new HashSet<>();

      for (DescriptiveMetadata metadata : rep.getDescriptiveMetadata()) {
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aip.getId(), rep.getId(), metadata.getId());
        Binary binary = model.getStorage().getBinary(storagePath);
        try {
          SolrInputDocument fields = getDescriptiveMetadataFields(binary, metadata.getType(), metadata.getVersion());
          for (SolrInputField field : fields) {
            if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
              boolean added = usedNonRepeatableFields.add(field.getName());
              if (added) {
                doc.addField(field.getName(), field.getValue(), field.getBoost());
              }
            } else {
              doc.addField(field.getName(), field.getValue(), field.getBoost());
            }
          }
        } catch (GenericException e) {
          LOGGER.info("Problem processing descriptive metadata: {}", e.getMessage());
        } catch (Exception e) {
          LOGGER.error("Error processing descriptive metadata: {}", metadata, e);
        }
      }
    }

    return doc;
  }

  public static SolrInputDocument fileToSolrDocument(AIP aip, File file, List<String> ancestors) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, IdUtils.getFileId(file));
    List<String> path = file.getPath();
    doc.addField(RodaConstants.FILE_PATH, path);
    if (path != null && !path.isEmpty()) {
      List<String> ancestorsPath = getFileAncestorsPath(file.getAipId(), file.getRepresentationId(), path);
      if (!ancestorsPath.isEmpty()) {
        doc.addField(RodaConstants.FILE_PARENT_UUID, ancestorsPath.get(ancestorsPath.size() - 1));
        doc.addField(RodaConstants.FILE_ANCESTORS_PATH, ancestorsPath);
      }
    }
    doc.addField(RodaConstants.FILE_AIP_ID, file.getAipId());
    doc.addField(RodaConstants.FILE_FILE_ID, file.getId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_UUID,
      IdUtils.getRepresentationId(file.getAipId(), file.getRepresentationId()));
    doc.addField(RodaConstants.FILE_ISDIRECTORY, file.isDirectory());

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getFileStoragePath(file);
      doc.addField(RodaConstants.FILE_STORAGEPATH, FSUtils.getStoragePathAsString(filePath, false));
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index file storage path", e);
    }

    String fileId = file.getId();
    if (!file.isDirectory() && fileId.contains(".") && !fileId.startsWith(".")) {
      String extension = fileId.substring(fileId.lastIndexOf('.') + 1);
      doc.addField(RodaConstants.FILE_EXTENSION, extension);
    }

    // indexing AIP inherited info
    doc.addField(RodaConstants.STATE, aip.getState().toString());
    doc.addField(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
    doc.addField(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
    doc.addField(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());
    doc.addField(RodaConstants.FILE_ANCESTORS, ancestors);
    setPermissions(aip.getPermissions(), doc);

    return doc;
  }

  private static List<String> getFileAncestorsPath(String aipId, String representationId, List<String> path) {
    List<String> parentFileDirectoryPath = new ArrayList<>();
    List<String> ancestorsPath = new ArrayList<>();

    parentFileDirectoryPath.addAll(path);

    while (!parentFileDirectoryPath.isEmpty()) {
      int lastElementIndex = parentFileDirectoryPath.size() - 1;
      String parentFileId = parentFileDirectoryPath.get(lastElementIndex);
      parentFileDirectoryPath.remove(lastElementIndex);
      ancestorsPath.add(0, IdUtils.getFileId(aipId, representationId, parentFileDirectoryPath, parentFileId));
    }

    return ancestorsPath;
  }

  public static IndexedFile solrDocumentToIndexedFile(SolrDocument doc, List<String> fieldsToReturn) {
    String uuid = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    String aipId = objectToString(doc.get(RodaConstants.FILE_AIP_ID), null);
    String representationId = objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_ID), null);
    String fileId = objectToString(doc.get(RodaConstants.FILE_FILE_ID), null);
    List<String> path = objectToListString(doc.get(RodaConstants.FILE_PATH));

    String representationUUID = objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_UUID), null);
    String parentUUID = objectToString(doc.get(RodaConstants.FILE_PARENT_UUID), null);
    List<String> ancestorsPath = objectToListString(doc.get(RodaConstants.FILE_ANCESTORS_PATH));

    String originalName = objectToString(doc.get(RodaConstants.FILE_ORIGINALNAME), null);
    List<String> hash = objectToListString(doc.get(RodaConstants.FILE_HASH));
    long size = objectToLong(doc.get(RodaConstants.FILE_SIZE), 0L);
    boolean isDirectory = objectToBoolean(doc.get(RodaConstants.FILE_ISDIRECTORY), Boolean.FALSE);
    String storagePath = objectToString(doc.get(RodaConstants.FILE_STORAGEPATH), null);

    // format
    String formatDesignationName = objectToString(doc.get(RodaConstants.FILE_FILEFORMAT), null);
    String formatDesignationVersion = objectToString(doc.get(RodaConstants.FILE_FORMAT_VERSION), null);
    String mimetype = objectToString(doc.get(RodaConstants.FILE_FORMAT_MIMETYPE), null);
    String pronom = objectToString(doc.get(RodaConstants.FILE_PRONOM), null);
    String extension = objectToString(doc.get(RodaConstants.FILE_EXTENSION), null);
    // FIXME how to restore format registries
    Map<String, String> formatRegistries = new HashMap<>();

    // technical features
    String creatingApplicationName = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_NAME), null);
    String creatingApplicationVersion = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION), null);
    String dateCreatedByApplication = objectToString(doc.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION), null);
    final List<String> ancestors = objectToListString(doc.get(RodaConstants.AIP_ANCESTORS));

    // handle other properties
    Map<String, List<String>> otherProperties = new HashMap<>();
    for (String fieldName : doc.getFieldNames()) {
      if (fieldName.endsWith("_txt")) {
        List<String> otherProperty = objectToListString(doc.get(fieldName));
        otherProperties.put(fieldName, otherProperty);
      }

    }

    FileFormat fileFormat = new FileFormat(formatDesignationName, formatDesignationVersion, mimetype, pronom, extension,
      formatRegistries);

    Map<String, Object> indexedFields = new HashMap<>();
    for (String field : fieldsToReturn) {
      indexedFields.put(field, doc.get(field));
    }

    return new IndexedFile(uuid, parentUUID, aipId, representationId, representationUUID, path, ancestorsPath, fileId,
      fileFormat, originalName, size, isDirectory, creatingApplicationName, creatingApplicationVersion,
      dateCreatedByApplication, hash, storagePath, ancestors, otherProperties).setFields(indexedFields);
  }

  public static SolrInputDocument addOtherPropertiesToIndexedFile(String prefix, OtherMetadata otherMetadataBinary,
    ModelService model, SolrClient index)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    ParserConfigurationException, SAXException, IOException, XPathExpressionException, SolrServerException {
    SolrDocument solrDocument = index.getById(RodaConstants.INDEX_FILE,
      IdUtils.getFileId(otherMetadataBinary.getAipId(), otherMetadataBinary.getRepresentationId(),
        otherMetadataBinary.getFileDirectoryPath(), otherMetadataBinary.getFileId()));

    Binary binary = model.retrieveOtherMetadataBinary(otherMetadataBinary);
    Map<String, List<String>> otherProperties = MetadataFileUtils.parseBinary(binary);

    for (Map.Entry<String, List<String>> entry : otherProperties.entrySet()) {
      solrDocument.setField(prefix + entry.getKey(), entry.getValue());
    }

    return solrDocumentToSolrInputDocument(solrDocument);
  }

  private static LogEntry solrDocumentToLogEntry(SolrDocument doc, List<String> fieldsToReturn) {
    final String id = objectToString(doc.get(RodaConstants.LOG_ID), null);
    final String actionComponent = objectToString(doc.get(RodaConstants.LOG_ACTION_COMPONENT), null);
    final String actionMethod = objectToString(doc.get(RodaConstants.LOG_ACTION_METHOD), null);
    final String address = objectToString(doc.get(RodaConstants.LOG_ADDRESS), null);
    final Date datetime = objectToDate(doc.get(RodaConstants.LOG_DATETIME));
    final long duration = objectToLong(doc.get(RodaConstants.LOG_DURATION), 0L);

    final String parameters = objectToString(doc.get(RodaConstants.LOG_PARAMETERS), null);
    final String relatedObjectId = objectToString(doc.get(RodaConstants.LOG_RELATED_OBJECT_ID), null);
    final String username = objectToString(doc.get(RodaConstants.LOG_USERNAME), null);
    LOG_ENTRY_STATE state = null;

    if (doc.containsKey(RodaConstants.LOG_STATE)) {
      state = LOG_ENTRY_STATE
        .valueOf(objectToString(doc.get(RodaConstants.LOG_STATE), LOG_ENTRY_STATE.UNKNOWN.toString()));
    }

    LogEntry entry = new LogEntry();
    entry.setId(id);
    entry.setActionComponent(actionComponent);
    entry.setActionMethod(actionMethod);
    entry.setAddress(address);
    entry.setDatetime(datetime);
    entry.setDuration(duration);
    entry.setState(state);
    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.LOG_PARAMETERS)) {
        entry.setParameters(JsonUtils.getListFromJson(parameters == null ? "" : parameters, LogEntryParameter.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing log entry parameters", e);
    }

    entry.setRelatedObjectID(relatedObjectId);
    entry.setUsername(username);
    return entry;
  }

  public static SolrInputDocument logEntryToSolrDocument(LogEntry logEntry) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, logEntry.getId());
    doc.addField(RodaConstants.LOG_ID, logEntry.getId());
    doc.addField(RodaConstants.LOG_ACTION_COMPONENT, logEntry.getActionComponent());
    doc.addField(RodaConstants.LOG_ACTION_METHOD, logEntry.getActionMethod());
    doc.addField(RodaConstants.LOG_ADDRESS, logEntry.getAddress());
    doc.addField(RodaConstants.LOG_DATETIME, logEntry.getDatetime());
    doc.addField(RodaConstants.LOG_DURATION, logEntry.getDuration());
    doc.addField(RodaConstants.LOG_PARAMETERS, JsonUtils.getJsonFromObject(logEntry.getParameters()));
    doc.addField(RodaConstants.LOG_RELATED_OBJECT_ID, logEntry.getRelatedObjectID());
    doc.addField(RodaConstants.LOG_USERNAME, logEntry.getUsername());
    doc.addField(RodaConstants.LOG_STATE, logEntry.getState().toString());

    return doc;
  }

  public static SolrInputDocument rodaMemberToSolrDocument(RODAMember member) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, member.getId());
    doc.addField(RodaConstants.MEMBERS_ID, member.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, member.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, member.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, member.getName());

    if (member.getDirectRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_DIRECT, new ArrayList<String>(member.getDirectRoles()));
    }
    if (member.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(member.getAllRoles()));
    }

    if (StringUtils.isNotBlank(member.getFullName())) {
      doc.addField(RodaConstants.MEMBERS_FULLNAME, member.getFullName());
    }

    // Add user specific fields
    if (member instanceof User) {
      User user = (User) member;
      doc.addField(RodaConstants.MEMBERS_EMAIL, user.getEmail());
      if (user.getGroups() != null) {
        doc.addField(RodaConstants.MEMBERS_GROUPS, new ArrayList<String>(user.getGroups()));
      }
    }

    // Add group specific fields
    if (member instanceof Group) {
      Group group = (Group) member;
      if (group.getUsers() != null) {
        doc.addField(RodaConstants.MEMBERS_USERS, new ArrayList<String>(group.getUsers()));
      }
    }

    return doc;
  }

  private static RODAMember solrDocumentToRodaMember(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    final String name = objectToString(doc.get(RodaConstants.MEMBERS_NAME), null);

    final boolean isActive = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE), Boolean.FALSE);
    final boolean isUser = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER), Boolean.FALSE);
    final String fullName = objectToString(doc.get(RodaConstants.MEMBERS_FULLNAME), null);

    final String email = objectToString(doc.get(RodaConstants.MEMBERS_EMAIL), null);
    final Set<String> groups = new HashSet<>(objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS)));
    final Set<String> users = new HashSet<>(objectToListString(doc.get(RodaConstants.MEMBERS_USERS)));
    final Set<String> directRoles = new HashSet<>(objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_DIRECT)));
    final Set<String> allRoles = new HashSet<>(objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL)));
    if (isUser) {
      User user = new User();
      user.setId(id);
      user.setName(name);

      user.setActive(isActive);
      user.setFullName(fullName);
      user.setDirectRoles(directRoles);
      user.setAllRoles(allRoles);

      user.setEmail(email);
      user.setGroups(groups);

      return user;
    } else {
      Group group = new Group();
      group.setId(id);
      group.setName(name);

      group.setActive(isActive);
      group.setFullName(fullName);
      group.setDirectRoles(directRoles);
      group.setAllRoles(allRoles);

      group.setUsers(users);

      return group;
    }
  }

  private static IndexedPreservationEvent solrDocumentToIndexedPreservationEvent(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    final String aipId = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_AIP_ID), null);
    final String representationUUID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID),
      null);
    final String fileUUID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_FILE_UUID), null);

    IndexedPreservationEvent ipe = new IndexedPreservationEvent();
    ipe.setId(id);
    ipe.setAipID(aipId);
    ipe.setRepresentationUUID(representationUUID);
    ipe.setFileUUID(fileUUID);

    String objectClass = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS), null);
    if (StringUtils.isNotBlank(objectClass)) {
      ipe.setObjectClass(PreservationMetadataEventClass.valueOf(objectClass.toUpperCase()));
    }

    final Date eventDateTime = objectToDate(doc.get(RodaConstants.PRESERVATION_EVENT_DATETIME));
    final String eventDetail = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_DETAIL), "");
    final String eventType = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_TYPE), "");
    final String eventOutcome = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME), "");

    final List<String> agents = objectToListString(doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER));
    final List<String> outcomes = objectToListString(
      doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER));
    final List<String> sources = objectToListString(
      doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER));

    ipe.setEventDateTime(eventDateTime);
    ipe.setEventDetail(eventDetail);
    ipe.setEventType(eventType);
    ipe.setEventOutcome(eventOutcome);

    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String source : sources) {
        ids.add(JsonUtils.getObjectFromJson(source, LinkingIdentifier.class));
      }
      ipe.setSourcesObjectIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking source", e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String outcome : outcomes) {
        ids.add(JsonUtils.getObjectFromJson(outcome, LinkingIdentifier.class));
      }
      ipe.setOutcomeObjectIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking outcome", e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String agent : agents) {
        ids.add(JsonUtils.getObjectFromJson(agent, LinkingIdentifier.class));
      }
      ipe.setLinkingAgentIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking agents", e);
    }
    return ipe;
  }

  private static IndexedPreservationAgent solrDocumentToIndexedPreservationAgent(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    final String name = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NAME), null);
    final String type = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_TYPE), null);
    final String extension = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_EXTENSION), null);
    final String version = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_VERSION), null);
    final String note = objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NOTE), null);
    final List<String> roles = objectToListString(doc.get(RodaConstants.PRESERVATION_AGENT_ROLES));

    IndexedPreservationAgent ipa = new IndexedPreservationAgent();
    ipa.setId(id);
    ipa.setName(name);
    ipa.setType(type);
    ipa.setExtension(extension);
    ipa.setVersion(version);
    ipa.setNote(note);
    ipa.setRoles(roles);
    return ipa;
  }

  private static TransferredResource solrDocumentToTransferredResource(SolrDocument doc) {
    String uuid = objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    String fullPath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH), null);

    String id = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ID), null);
    String parentId = null;
    String parentUUID = null;
    if (doc.containsKey(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)) {
      parentId = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID), null);
      parentUUID = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID), null);
    }
    String relativePath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH), null);

    Date d = objectToDate(doc.get(RodaConstants.TRANSFERRED_RESOURCE_DATE));
    if (d == null) {
      LOGGER.warn("Error parsing transferred resource date. Setting date to current date.");
      d = new Date();
    }

    boolean isFile = objectToBoolean(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ISFILE), Boolean.FALSE);
    long size = objectToLong(doc.get(RodaConstants.TRANSFERRED_RESOURCE_SIZE), 0L);
    String name = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_NAME), null);

    List<String> ancestorsPath = objectToListString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS));

    Date lastScanDate = objectToDate(doc.get(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE));

    TransferredResource tr = new TransferredResource();
    tr.setUUID(uuid);
    tr.setFullPath(fullPath);
    tr.setId(id);
    tr.setCreationDate(d);
    tr.setName(name);
    tr.setRelativePath(relativePath);
    tr.setSize(size);
    tr.setParentId(parentId);
    tr.setParentUUID(parentUUID);
    tr.setFile(isFile);
    tr.setAncestorsPaths(ancestorsPath);
    tr.setLastScanDate(lastScanDate);
    return tr;
  }

  public static SolrInputDocument transferredResourceToSolrDocument(TransferredResource resource) {
    SolrInputDocument transferredResource = new SolrInputDocument();

    transferredResource.addField(RodaConstants.INDEX_UUID, resource.getUUID());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ID, resource.getId());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH, resource.getFullPath());
    if (resource.getParentId() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, resource.getParentId());
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID,
        IdUtils.createUUID(resource.getParentId()));
    }
    if (resource.getRelativePath() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, resource.getRelativePath());
    }
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_DATE, resource.getCreationDate());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, resource.isFile());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_SIZE, resource.getSize());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_NAME, resource.getName());
    if (resource.getAncestorsPaths() != null && !resource.getAncestorsPaths().isEmpty()) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, resource.getAncestorsPaths());
    }
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE, resource.getLastScanDate());

    return transferredResource;
  }

  public static SolrInputDocument jobToSolrDocument(Job job) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, job.getId());
    doc.addField(RodaConstants.JOB_ID, job.getId());
    doc.addField(RodaConstants.JOB_NAME, job.getName());
    doc.addField(RodaConstants.JOB_USERNAME, job.getUsername());
    doc.addField(RodaConstants.JOB_START_DATE, job.getStartDate());
    doc.addField(RodaConstants.JOB_END_DATE, job.getEndDate());
    doc.addField(RodaConstants.JOB_STATE, job.getState().toString());
    doc.addField(RodaConstants.JOB_STATE_DETAILS, job.getStateDetails());
    JobStats jobStats = job.getJobStats();
    doc.addField(RodaConstants.JOB_COMPLETION_PERCENTAGE, jobStats.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_COUNT, jobStats.getSourceObjectsCount());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED,
      jobStats.getSourceObjectsWaitingToBeProcessed());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_BEING_PROCESSED, jobStats.getSourceObjectsBeingProcessed());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS,
      jobStats.getSourceObjectsProcessedWithSuccess());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE,
      jobStats.getSourceObjectsProcessedWithFailure());
    doc.addField(RodaConstants.JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION,
      jobStats.getOutcomeObjectsWithManualIntervention());
    doc.addField(RodaConstants.JOB_PLUGIN_TYPE, job.getPluginType().toString());
    doc.addField(RodaConstants.JOB_PLUGIN, job.getPlugin());
    doc.addField(RodaConstants.JOB_PLUGIN_PARAMETERS, JsonUtils.getJsonFromObject(job.getPluginParameters()));
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS, JsonUtils.getJsonFromObject(job.getSourceObjects()));
    doc.addField(RodaConstants.JOB_OUTCOME_OBJECTS_CLASS, job.getOutcomeObjectsClass());

    return doc;
  }

  public static Job solrDocumentToJob(SolrDocument doc, List<String> fieldsToReturn) {
    Job job = new Job();

    job.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    job.setName(objectToString(doc.get(RodaConstants.JOB_NAME), null));
    job.setUsername(objectToString(doc.get(RodaConstants.JOB_USERNAME), null));
    job.setStartDate(objectToDate(doc.get(RodaConstants.JOB_START_DATE)));
    job.setEndDate(objectToDate(doc.get(RodaConstants.JOB_END_DATE)));
    if (doc.containsKey(RodaConstants.JOB_STATE)) {
      job.setState(JOB_STATE.valueOf(objectToString(doc.get(RodaConstants.JOB_STATE), null)));
    }
    job.setStateDetails(objectToString(doc.get(RodaConstants.JOB_STATE_DETAILS), null));
    JobStats jobStats = job.getJobStats();
    jobStats.setCompletionPercentage(objectToInteger(doc.get(RodaConstants.JOB_COMPLETION_PERCENTAGE), 0));
    jobStats.setSourceObjectsCount(objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_COUNT), 0));
    jobStats.setSourceObjectsWaitingToBeProcessed(
      objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED), 0));
    jobStats
      .setSourceObjectsBeingProcessed(objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_BEING_PROCESSED), 0));
    jobStats.setSourceObjectsProcessedWithSuccess(
      objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS), 0));
    jobStats.setSourceObjectsProcessedWithFailure(
      objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE), 0));
    jobStats.setOutcomeObjectsWithManualIntervention(
      objectToInteger(doc.get(RodaConstants.JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION), 0));
    if (doc.containsKey(RodaConstants.JOB_PLUGIN_TYPE)) {
      job.setPluginType(PluginType.valueOf(objectToString(doc.get(RodaConstants.JOB_PLUGIN_TYPE), null)));
    }
    job.setPlugin(objectToString(doc.get(RodaConstants.JOB_PLUGIN), null));
    if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_PLUGIN_PARAMETERS)) {
      job.setPluginParameters(
        JsonUtils.getMapFromJson(objectToString(doc.get(RodaConstants.JOB_PLUGIN_PARAMETERS), "")));
    }

    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_SOURCE_OBJECTS)) {
        job.setSourceObjects(JsonUtils.getObjectFromJson(objectToString(doc.get(RodaConstants.JOB_SOURCE_OBJECTS), ""),
          SelectedItems.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing report in job objects", e);
    }
    job.setOutcomeObjectsClass(objectToString(doc.get(RodaConstants.JOB_OUTCOME_OBJECTS_CLASS), null));

    return job;
  }

  public static <T extends Serializable> SolrInputDocument jobReportToSolrDocument(Report jobReport, Job job,
    SolrClient index) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, jobReport.getId());
    doc.addField(RodaConstants.JOB_REPORT_ID, jobReport.getId());
    doc.addField(RodaConstants.JOB_REPORT_JOB_ID, jobReport.getJobId());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, jobReport.getSourceObjectId());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, jobReport.getSourceObjectOriginalIds());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME, jobReport.getSourceObjectOriginalName());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, jobReport.getOutcomeObjectId());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE, jobReport.getOutcomeObjectState().toString());
    doc.addField(RodaConstants.JOB_REPORT_TITLE, jobReport.getTitle());
    doc.addField(RodaConstants.JOB_REPORT_DATE_CREATED, jobReport.getDateCreated());
    doc.addField(RodaConstants.JOB_REPORT_DATE_UPDATED, jobReport.getDateUpdated());
    doc.addField(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE, jobReport.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_REPORT_STEPS_COMPLETED, jobReport.getStepsCompleted());
    doc.addField(RodaConstants.JOB_REPORT_TOTAL_STEPS, jobReport.getTotalSteps());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN, jobReport.getPlugin());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_NAME, jobReport.getPluginName());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_VERSION, jobReport.getPluginVersion());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_STATE, jobReport.getPluginState().toString());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_DETAILS, jobReport.getPluginDetails());
    doc.addField(RodaConstants.JOB_REPORT_HTML_PLUGIN_DETAILS, jobReport.isHtmlPluginDetails());
    doc.addField(RodaConstants.JOB_REPORT_REPORTS, JsonUtils.getJsonFromObject(jobReport.getReports()));
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, jobReport.getSourceObjectClass());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS, jobReport.getOutcomeObjectClass());
    doc.addField(RodaConstants.JOB_REPORT_JOB_NAME, job.getName());

    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL,
      getObjectLabel(index, jobReport.getSourceObjectClass(), jobReport.getSourceObjectId()));
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL,
      getObjectLabel(index, jobReport.getOutcomeObjectClass(), jobReport.getOutcomeObjectId()));

    return doc;
  }

  private static <T extends Serializable> String getObjectLabel(SolrClient index, String className, String id) {
    if (StringUtils.isNotBlank(className) && StringUtils.isNotBlank(id)) {
      try {
        Class<T> objectClass = (Class<T>) Class.forName(className);
        if (objectClass.equals(LiteRODAObject.class)) {
          return null;
        }
        String field = getIndexName(objectClass).get(0);
        SolrDocument doc = index.getById(field, id);
        if (doc != null) {
          if (objectClass.equals(AIP.class)) {
            return objectToString(doc.get(RodaConstants.AIP_TITLE), null);
          } else if (objectClass.equals(Representation.class)) {
            return objectToString(doc.get(RodaConstants.REPRESENTATION_ID), null);
          } else if (objectClass.equals(File.class)) {
            return objectToString(doc.get(RodaConstants.FILE_FILE_ID), null);
          } else if (objectClass.equals(TransferredResource.class)) {
            return objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ID), null);
          } else if (objectClass.equals(DIP.class)) {
            return objectToString(doc.get(RodaConstants.DIP_TITLE), null);
          } else if (objectClass.equals(DIPFile.class)) {
            return objectToString(doc.get(RodaConstants.DIPFILE_ID), null);
          } else {
            return objectToString(doc.get(RodaConstants.INDEX_UUID), null);
          }
        }
      } catch (ClassNotFoundException | GenericException | SolrServerException | IOException e) {
        LOGGER.error("Could not return object label of {} {}", className, id, e);
      }
    }

    return null;
  }

  private static IndexedReport solrDocumentToJobReport(SolrDocument doc, List<String> fieldsToReturn) {
    IndexedReport jobReport = new IndexedReport();
    jobReport.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    jobReport.setJobId(objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_ID), null));
    jobReport.setSourceObjectId(objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID), null));
    jobReport.setSourceObjectClass(objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS), null));
    jobReport
      .setSourceObjectOriginalIds(objectToListString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS)));
    jobReport
      .setSourceObjectOriginalName(objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME), null));
    jobReport.setOutcomeObjectId(objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID), null));
    jobReport.setOutcomeObjectClass(objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS), null));
    if (doc.containsKey(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE)) {
      jobReport.setOutcomeObjectState(AIPState.valueOf(
        objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE), AIPState.getDefault().toString())));
    }
    jobReport.setTitle(objectToString(doc.get(RodaConstants.JOB_REPORT_TITLE), null));
    jobReport.setDateCreated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_CREATED)));
    jobReport.setDateUpdated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_UPDATED)));
    jobReport.setCompletionPercentage(objectToInteger(doc.get(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE), 0));
    jobReport.setStepsCompleted(objectToInteger(doc.get(RodaConstants.JOB_REPORT_STEPS_COMPLETED), 0));
    jobReport.setTotalSteps(objectToInteger(doc.get(RodaConstants.JOB_REPORT_TOTAL_STEPS), 0));
    jobReport.setPlugin(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN), null));
    jobReport.setPluginName(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_NAME), null));
    jobReport.setPluginVersion(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_VERSION), null));
    jobReport.setPluginState(PluginState.valueOf(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_STATE), null)));
    jobReport.setPluginDetails(objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_DETAILS), null));
    jobReport.setHtmlPluginDetails(objectToBoolean(doc.get(RodaConstants.JOB_REPORT_HTML_PLUGIN_DETAILS), false));
    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_REPORT_REPORTS)) {
        jobReport.setReports(
          JsonUtils.getListFromJson(objectToString(doc.get(RodaConstants.JOB_REPORT_REPORTS), ""), Report.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing report in job report", e);
    }

    jobReport.setJobName(objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_NAME), null));
    jobReport.setSourceObjectLabel(objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL), null));
    jobReport.setOutcomeObjectLabel(objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL), null));

    return jobReport;
  }

  public static SolrInputDocument riskToSolrDocument(Risk risk, int incidences) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, risk.getId());
    doc.addField(RodaConstants.RISK_ID, risk.getId());
    doc.addField(RodaConstants.RISK_NAME, risk.getName());
    doc.addField(RodaConstants.RISK_DESCRIPTION, risk.getDescription());
    doc.addField(RodaConstants.RISK_IDENTIFIED_ON, risk.getIdentifiedOn());
    doc.addField(RodaConstants.RISK_IDENTIFIED_BY, risk.getIdentifiedBy());
    doc.addField(RodaConstants.RISK_CATEGORY, risk.getCategory());
    doc.addField(RodaConstants.RISK_NOTES, risk.getNotes());

    doc.addField(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, risk.getPreMitigationProbability());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_IMPACT, risk.getPreMitigationImpact());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_SEVERITY, risk.getPreMitigationSeverity());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL, risk.getPreMitigationSeverityLevel().toString());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_NOTES, risk.getPreMitigationNotes());

    doc.addField(RodaConstants.RISK_POST_MITIGATION_PROBABILITY, risk.getPostMitigationProbability());
    doc.addField(RodaConstants.RISK_POST_MITIGATION_IMPACT, risk.getPostMitigationImpact());
    doc.addField(RodaConstants.RISK_POST_MITIGATION_SEVERITY, risk.getPostMitigationSeverity());

    if (risk.getPostMitigationSeverityLevel() != null) {
      doc.addField(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL, risk.getPostMitigationSeverityLevel().toString());
    }

    doc.addField(RodaConstants.RISK_CURRENT_SEVERITY_LEVEL, risk.getCurrentSeverityLevel());

    doc.addField(RodaConstants.RISK_POST_MITIGATION_NOTES, risk.getPostMitigationNotes());

    doc.addField(RodaConstants.RISK_MITIGATION_STRATEGY, risk.getMitigationStrategy());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER_TYPE, risk.getMitigationOwnerType());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER, risk.getMitigationOwner());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
      risk.getMitigationRelatedEventIdentifierType());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE,
      risk.getMitigationRelatedEventIdentifierValue());

    doc.addField(RodaConstants.RISK_CREATED_ON, risk.getCreatedOn());
    doc.addField(RodaConstants.RISK_CREATED_BY, risk.getCreatedBy());
    doc.addField(RodaConstants.RISK_UPDATED_ON, risk.getUpdatedOn());
    doc.addField(RodaConstants.RISK_UPDATED_BY, risk.getUpdatedBy());

    if (risk instanceof IndexedRisk) {
      doc.addField(RodaConstants.RISK_INCIDENCES_COUNT, ((IndexedRisk) risk).getIncidencesCount());
      doc.addField(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT,
        ((IndexedRisk) risk).getUnmitigatedIncidencesCount());
    } else {
      doc.addField(RodaConstants.RISK_INCIDENCES_COUNT, incidences);
      doc.addField(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT, incidences);
    }

    return doc;
  }

  public static IndexedRisk solrDocumentToRisk(SolrDocument doc) {
    IndexedRisk risk = new IndexedRisk();

    risk.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    risk.setName(objectToString(doc.get(RodaConstants.RISK_NAME), null));
    risk.setDescription(objectToString(doc.get(RodaConstants.RISK_DESCRIPTION), null));
    risk.setIdentifiedOn(objectToDate(doc.get(RodaConstants.RISK_IDENTIFIED_ON)));
    risk.setIdentifiedBy(objectToString(doc.get(RodaConstants.RISK_IDENTIFIED_BY), null));
    risk.setCategory(objectToString(doc.get(RodaConstants.RISK_CATEGORY), null));
    risk.setNotes(objectToString(doc.get(RodaConstants.RISK_NOTES), null));

    risk.setPreMitigationProbability(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY), 0));
    risk.setPreMitigationImpact(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_IMPACT), 0));
    risk.setPreMitigationSeverity(objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_SEVERITY), 0));
    if (doc.containsKey(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL)) {
      risk.setPreMitigationSeverityLevel(
        objectToEnum(doc.get(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL), Risk.SEVERITY_LEVEL.class, null));
    }
    risk.setPreMitigationNotes(objectToString(doc.get(RodaConstants.RISK_PRE_MITIGATION_NOTES), null));

    risk.setPostMitigationProbability(objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_PROBABILITY), 0));
    risk.setPostMitigationImpact(objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_IMPACT), 0));
    risk.setPostMitigationSeverity(objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_SEVERITY), 0));
    if (doc.containsKey(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL)) {
      risk.setPostMitigationSeverityLevel(
        objectToEnum(doc.get(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL), Risk.SEVERITY_LEVEL.class, null));
    }
    risk.setPostMitigationNotes(objectToString(doc.get(RodaConstants.RISK_POST_MITIGATION_NOTES), null));

    risk.setMitigationStrategy(objectToString(doc.get(RodaConstants.RISK_MITIGATION_STRATEGY), null));
    risk.setMitigationOwnerType(objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER_TYPE), null));
    risk.setMitigationOwner(objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER), null));
    risk.setMitigationRelatedEventIdentifierType(
      objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE), null));
    risk.setMitigationRelatedEventIdentifierValue(
      objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE), null));

    risk.setCreatedOn(objectToDate(doc.get(RodaConstants.RISK_CREATED_ON)));
    risk.setCreatedBy(objectToString(doc.get(RodaConstants.RISK_CREATED_BY), null));
    risk.setUpdatedOn(objectToDate(doc.get(RodaConstants.RISK_UPDATED_ON)));
    risk.setUpdatedBy(objectToString(doc.get(RodaConstants.RISK_UPDATED_BY), null));

    risk.setIncidencesCount(objectToInteger(doc.get(RodaConstants.RISK_INCIDENCES_COUNT), 0));
    risk.setUnmitigatedIncidencesCount(objectToInteger(doc.get(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT), 0));
    return risk;
  }

  public static SolrInputDocument representationInformationToSolrDocument(RepresentationInformation ri) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, ri.getId());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_ID, ri.getId());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_NAME, ri.getName());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_NAME_SORT, ri.getName());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION, ri.getDescription());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FAMILY, ri.getFamily());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES, ri.getCategories());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES_SORT,
      (ri.getCategories() != null && !ri.getCategories().isEmpty()) ? ri.getCategories().get(0) : null);
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_EXTRAS, ri.getExtras());

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, ri.getSupport().toString());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS, JsonUtils.getJsonFromObject(ri.getRelations()));

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, ri.getFilters());

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON, ri.getCreatedOn());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY, ri.getCreatedBy());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, ri.getUpdatedOn());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY, ri.getUpdatedBy());

    return doc;
  }

  public static RepresentationInformation solrDocumentToRepresentationInformation(SolrDocument doc) {
    RepresentationInformation ri = new RepresentationInformation();
    ri.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    ri.setName(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_NAME), null));
    ri.setDescription(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION), null));
    ri.setFamily(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_FAMILY), null));
    ri.setCategories(objectToListString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES)));
    ri.setExtras(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_EXTRAS), null));

    ri.setSupport(objectToEnum(doc.get(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT),
      RepresentationInformationSupport.class, RepresentationInformationSupport.KNOWN));
    ri.setRelations(objectToListRelation(doc.get(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS)));

    ri.setFilters(objectToListString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_FILTERS)));

    ri.setCreatedOn(objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON)));
    ri.setCreatedBy(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY), ""));
    ri.setUpdatedOn(objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON)));
    ri.setUpdatedBy(objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY), ""));

    return ri;
  }

  public static SolrInputDocument formatToSolrDocument(Format format) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.INDEX_UUID, format.getId());
    doc.addField(RodaConstants.FORMAT_ID, format.getId());
    doc.addField(RodaConstants.FORMAT_NAME, format.getName());
    doc.addField(RodaConstants.FORMAT_NAME_SORT, format.getName());
    doc.addField(RodaConstants.FORMAT_DEFINITION, format.getDefinition());
    doc.addField(RodaConstants.FORMAT_CATEGORY, format.getCategories());
    doc.addField(RodaConstants.FORMAT_CATEGORY_SORT,
      (format.getCategories() != null && !format.getCategories().isEmpty()) ? format.getCategories().get(0) : null);
    doc.addField(RodaConstants.FORMAT_LATEST_VERSION, format.getLatestVersion());
    if (format.getPopularity() != null) {
      doc.addField(RodaConstants.FORMAT_POPULARITY, format.getPopularity());
    }
    doc.addField(RodaConstants.FORMAT_DEVELOPER, format.getDeveloper());
    doc.addField(RodaConstants.FORMAT_INITIAL_RELEASE, format.getInitialRelease());
    doc.addField(RodaConstants.FORMAT_STANDARD, format.getStandard());
    doc.addField(RodaConstants.FORMAT_IS_OPEN_FORMAT, format.isOpenFormat());
    doc.addField(RodaConstants.FORMAT_WEBSITE, format.getWebsites());
    doc.addField(RodaConstants.FORMAT_PROVENANCE_INFORMATION, format.getProvenanceInformation());
    doc.addField(RodaConstants.FORMAT_EXTENSIONS, format.getExtensions());
    doc.addField(RodaConstants.FORMAT_MIMETYPES, format.getMimetypes());
    doc.addField(RodaConstants.FORMAT_PRONOMS, format.getPronoms());
    doc.addField(RodaConstants.FORMAT_UTIS, format.getUtis());
    doc.addField(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS, format.getAlternativeDesignations());
    doc.addField(RodaConstants.FORMAT_VERSIONS, format.getVersions());

    return doc;
  }

  public static Format solrDocumentToFormat(SolrDocument doc) {
    Format format = new Format();
    format.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    format.setName(objectToString(doc.get(RodaConstants.FORMAT_NAME), null));
    format.setDefinition(objectToString(doc.get(RodaConstants.FORMAT_DEFINITION), null));
    format.setCategories(objectToListString(doc.get(RodaConstants.FORMAT_CATEGORY)));
    format.setLatestVersion(objectToString(doc.get(RodaConstants.FORMAT_LATEST_VERSION), null));
    format.setPopularity(objectToInteger(doc.get(RodaConstants.FORMAT_POPULARITY), null));
    format.setDeveloper(objectToString(doc.get(RodaConstants.FORMAT_DEVELOPER), null));
    format.setInitialRelease(objectToDate(doc.get(RodaConstants.FORMAT_INITIAL_RELEASE)));
    format.setStandard(objectToString(doc.get(RodaConstants.FORMAT_STANDARD), null));
    format.setOpenFormat(objectToBoolean(doc.get(RodaConstants.FORMAT_IS_OPEN_FORMAT), Boolean.FALSE));
    format.setWebsites(objectToListString(doc.get(RodaConstants.FORMAT_WEBSITE)));
    format.setProvenanceInformation(objectToString(doc.get(RodaConstants.FORMAT_PROVENANCE_INFORMATION), null));
    format.setExtensions(objectToListString(doc.get(RodaConstants.FORMAT_EXTENSIONS)));
    format.setMimetypes(objectToListString(doc.get(RodaConstants.FORMAT_MIMETYPES)));
    format.setPronoms(objectToListString(doc.get(RodaConstants.FORMAT_PRONOMS)));
    format.setUtis(objectToListString(doc.get(RodaConstants.FORMAT_UTIS)));
    format.setAlternativeDesignations(objectToListString(doc.get(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS)));
    format.setVersions(objectToListString(doc.get(RodaConstants.FORMAT_VERSIONS)));
    return format;
  }

  public static SolrInputDocument notificationToSolrDocument(Notification notification) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, notification.getId());
    doc.addField(RodaConstants.NOTIFICATION_ID, notification.getId());
    doc.addField(RodaConstants.NOTIFICATION_SUBJECT, notification.getSubject());
    doc.addField(RodaConstants.NOTIFICATION_BODY, notification.getBody());
    doc.addField(RodaConstants.NOTIFICATION_SENT_ON, notification.getSentOn());
    doc.addField(RodaConstants.NOTIFICATION_FROM_USER, notification.getFromUser());
    doc.addField(RodaConstants.NOTIFICATION_RECIPIENT_USERS, notification.getRecipientUsers());
    doc.addField(RodaConstants.NOTIFICATION_ACKNOWLEDGE_TOKEN, notification.getAcknowledgeToken());
    doc.addField(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED, notification.isAcknowledged());
    doc.addField(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS,
      JsonUtils.getJsonFromObject(notification.getAcknowledgedUsers()));
    doc.addField(RodaConstants.NOTIFICATION_STATE, notification.getState().toString());
    return doc;
  }

  public static Notification solrDocumentToNotification(SolrDocument doc, List<String> fieldsToReturn) {
    Notification notification = new Notification();

    notification.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    notification.setSubject(objectToString(doc.get(RodaConstants.NOTIFICATION_SUBJECT), null));
    notification.setBody(objectToString(doc.get(RodaConstants.NOTIFICATION_BODY), null));
    notification.setSentOn(objectToDate(doc.get(RodaConstants.NOTIFICATION_SENT_ON)));
    notification.setFromUser(objectToString(doc.get(RodaConstants.NOTIFICATION_FROM_USER), null));
    notification.setRecipientUsers(objectToListString(doc.get(RodaConstants.NOTIFICATION_RECIPIENT_USERS)));
    notification.setAcknowledgeToken(objectToString(doc.get(RodaConstants.NOTIFICATION_ACKNOWLEDGE_TOKEN), null));
    notification.setAcknowledged(objectToBoolean(doc.get(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED), Boolean.FALSE));
    if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS)) {
      notification.setAcknowledgedUsers(
        JsonUtils.getMapFromJson(objectToString(doc.get(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS), "")));
    }

    if (doc.containsKey(RodaConstants.NOTIFICATION_STATE)) {
      String defaultState = Notification.NOTIFICATION_STATE.COMPLETED.toString();
      notification.setState(Notification.NOTIFICATION_STATE
        .valueOf(objectToString(doc.get(RodaConstants.NOTIFICATION_STATE), defaultState)));
    }
    return notification;
  }

  public static SolrInputDocument riskIncidenceToSolrDocument(RiskIncidence incidence) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, incidence.getId());
    doc.addField(RodaConstants.RISK_INCIDENCE_ID, incidence.getId());
    doc.addField(RodaConstants.RISK_INCIDENCE_AIP_ID, incidence.getAipId());
    doc.addField(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, incidence.getRepresentationId());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_PATH, incidence.getFilePath());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_ID, incidence.getFileId());
    doc.addField(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS, incidence.getObjectClass());
    doc.addField(RodaConstants.RISK_INCIDENCE_RISK_ID, incidence.getRiskId());
    doc.addField(RodaConstants.RISK_INCIDENCE_DESCRIPTION, incidence.getDescription());
    doc.addField(RodaConstants.RISK_INCIDENCE_BYPLUGIN, incidence.isByPlugin());
    doc.addField(RodaConstants.RISK_INCIDENCE_STATUS, incidence.getStatus().toString());
    doc.addField(RodaConstants.RISK_INCIDENCE_SEVERITY, incidence.getSeverity().toString());
    doc.addField(RodaConstants.RISK_INCIDENCE_DETECTED_ON, incidence.getDetectedOn());
    doc.addField(RodaConstants.RISK_INCIDENCE_DETECTED_BY, incidence.getDetectedBy());
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_ON, incidence.getMitigatedOn());
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_BY, incidence.getMitigatedBy());
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION, incidence.getMitigatedDescription());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
      StringUtils.join(incidence.getFilePath(), RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR));
    return doc;
  }

  public static RiskIncidence solrDocumentToRiskIncidence(SolrDocument doc) {
    RiskIncidence incidence = new RiskIncidence();
    incidence.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    incidence.setAipId(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_AIP_ID), null));
    incidence.setRepresentationId(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID), null));
    incidence.setFilePath(objectToListString(doc.get(RodaConstants.RISK_INCIDENCE_FILE_PATH)));
    incidence.setFileId(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_FILE_ID), null));
    incidence.setObjectClass(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS), null));
    incidence.setRiskId(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_RISK_ID), null));
    incidence.setDescription(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_DESCRIPTION), null));
    if (doc.containsKey(RodaConstants.RISK_INCIDENCE_STATUS)) {
      incidence
        .setStatus(RiskIncidence.INCIDENCE_STATUS.valueOf(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_STATUS),
          RiskIncidence.INCIDENCE_STATUS.UNMITIGATED.toString())));
    }
    if (doc.containsKey(RodaConstants.RISK_INCIDENCE_SEVERITY)) {
      incidence.setSeverity(Risk.SEVERITY_LEVEL.valueOf(
        objectToString(doc.get(RodaConstants.RISK_INCIDENCE_SEVERITY), Risk.SEVERITY_LEVEL.MODERATE.toString())));
    }
    incidence.setDetectedOn(objectToDate(doc.get(RodaConstants.RISK_INCIDENCE_DETECTED_ON)));
    incidence.setDetectedBy(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_DETECTED_BY), null));
    incidence.setMitigatedOn(objectToDate(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_ON)));
    incidence.setMitigatedBy(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_BY), null));
    incidence
      .setMitigatedDescription(objectToString(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION), null));
    return incidence;
  }

  public static SolrInputDocument dipToSolrDocument(DIP dip) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, dip.getId());
    doc.addField(RodaConstants.DIP_ID, dip.getId());
    doc.addField(RodaConstants.DIP_TITLE, dip.getTitle());
    doc.addField(RodaConstants.DIP_DESCRIPTION, dip.getDescription());
    doc.addField(RodaConstants.DIP_TYPE, dip.getType());
    doc.addField(RodaConstants.DIP_DATE_CREATED, dip.getDateCreated());
    doc.addField(RodaConstants.DIP_LAST_MODIFIED, dip.getLastModified());
    doc.addField(RodaConstants.DIP_IS_PERMANENT, dip.getIsPermanent());
    doc.addField(RodaConstants.DIP_PROPERTIES, JsonUtils.getJsonFromObject(dip.getProperties()));

    doc.addField(RodaConstants.DIP_AIP_IDS, JsonUtils.getJsonFromObject(dip.getAipIds()));
    doc.addField(RodaConstants.DIP_REPRESENTATION_IDS, JsonUtils.getJsonFromObject(dip.getRepresentationIds()));
    doc.addField(RodaConstants.DIP_FILE_IDS, JsonUtils.getJsonFromObject(dip.getFileIds()));

    List<String> allAipUUIDs = new ArrayList<>();
    List<String> allRepresentationUUIDs = new ArrayList<>();

    List<String> aipUUIDs = new ArrayList<>();
    for (AIPLink aip : dip.getAipIds()) {
      aipUUIDs.add(aip.getAipId());
    }

    allAipUUIDs.addAll(aipUUIDs);

    List<String> representationUUIDs = new ArrayList<>();
    for (RepresentationLink rep : dip.getRepresentationIds()) {
      representationUUIDs.add(IdUtils.getRepresentationId(rep));
      if (!allAipUUIDs.contains(rep.getAipId())) {
        allAipUUIDs.add(rep.getAipId());
      }
    }

    allRepresentationUUIDs.addAll(representationUUIDs);

    List<String> fileUUIDs = new ArrayList<>();
    for (FileLink file : dip.getFileIds()) {
      fileUUIDs.add(IdUtils.getFileId(file));
      if (!allAipUUIDs.contains(file.getAipId())) {
        allAipUUIDs.add(file.getAipId());
      }

      String repUUID = IdUtils.getRepresentationId(file.getAipId(), file.getRepresentationId());
      if (!allRepresentationUUIDs.contains(repUUID)) {
        allRepresentationUUIDs.add(repUUID);
      }
    }

    doc.addField(RodaConstants.DIP_AIP_UUIDS, aipUUIDs);
    doc.addField(RodaConstants.DIP_REPRESENTATION_UUIDS, representationUUIDs);
    doc.addField(RodaConstants.DIP_FILE_UUIDS, fileUUIDs);

    doc.addField(RodaConstants.DIP_ALL_AIP_UUIDS, allAipUUIDs);
    doc.addField(RodaConstants.DIP_ALL_REPRESENTATION_UUIDS, allRepresentationUUIDs);

    setPermissions(dip.getPermissions(), doc);

    OptionalWithCause<String> openURL = DIPUtils.getCompleteOpenExternalURL(dip);
    if (openURL.isPresent()) {
      doc.addField(RodaConstants.DIP_OPEN_EXTERNAL_URL, openURL.get());
    } else if (openURL.getCause() != null) {
      LOGGER.error("Error indexing DIP open external URL", openURL.getCause());
    }

    return doc;
  }

  public static IndexedDIP solrDocumentToIndexedDIP(SolrDocument doc, List<String> fieldsToReturn) {
    IndexedDIP dip = new IndexedDIP();
    dip.setId(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    dip.setTitle(objectToString(doc.get(RodaConstants.DIP_TITLE), null));
    dip.setDescription(objectToString(doc.get(RodaConstants.DIP_DESCRIPTION), null));
    dip.setType(objectToString(doc.get(RodaConstants.DIP_TYPE), null));
    dip.setDateCreated(objectToDate(doc.get(RodaConstants.DIP_DATE_CREATED)));
    dip.setLastModified(objectToDate(doc.get(RodaConstants.DIP_LAST_MODIFIED)));
    dip.setIsPermanent(objectToBoolean(doc.get(RodaConstants.DIP_IS_PERMANENT), Boolean.FALSE));

    boolean emptyFields = fieldsToReturn.isEmpty();

    if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_PROPERTIES)) {
      dip.setProperties(JsonUtils.getMapFromJson(objectToString(doc.get(RodaConstants.DIP_PROPERTIES), "")));
    }

    try {
      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_AIP_IDS)) {
        String aipIds = objectToString(doc.get(RodaConstants.DIP_AIP_IDS), null);
        dip.setAipIds(JsonUtils.getListFromJson(aipIds == null ? "" : aipIds, AIPLink.class));
      }

      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_REPRESENTATION_IDS)) {
        String representationIds = objectToString(doc.get(RodaConstants.DIP_REPRESENTATION_IDS), null);
        dip.setRepresentationIds(
          JsonUtils.getListFromJson(representationIds == null ? "" : representationIds, RepresentationLink.class));
      }

      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_FILE_IDS)) {
        String fileIds = objectToString(doc.get(RodaConstants.DIP_FILE_IDS), null);
        dip.setFileIds(JsonUtils.getListFromJson(fileIds == null ? "" : fileIds, FileLink.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error getting related ids from DIP index");
    }

    dip.setPermissions(getPermissions(doc));
    dip.setOpenExternalURL(objectToString(doc.get(RodaConstants.DIP_OPEN_EXTERNAL_URL), null));
    return dip;
  }

  public static SolrInputDocument dipFileToSolrDocument(DIP dip, DIPFile file) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, IdUtils.getDIPFileId(file));
    List<String> path = file.getPath();
    doc.addField(RodaConstants.DIPFILE_PATH, path);
    if (path != null && !path.isEmpty()) {
      List<String> ancestorsPath = getDIPFileAncestorsPath(file.getDipId(), path);
      if (!ancestorsPath.isEmpty()) {
        doc.addField(RodaConstants.DIPFILE_ANCESTORS_UUIDS, ancestorsPath);
      }

      doc.addField(RodaConstants.DIPFILE_PARENT_UUID,
        IdUtils.getDIPFileId(file.getDipId(), path.subList(0, path.size() - 1), path.get(path.size() - 1)));
    }

    doc.addField(RodaConstants.DIPFILE_DIP_ID, file.getDipId());
    doc.addField(RodaConstants.DIPFILE_ID, file.getId());
    doc.addField(RodaConstants.DIPFILE_IS_DIRECTORY, file.isDirectory());
    doc.addField(RodaConstants.DIPFILE_SIZE, Long.toString(file.getSize()));

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getDIPFileStoragePath(file);
      doc.addField(RodaConstants.DIPFILE_STORAGE_PATH, FSUtils.getStoragePathAsString(filePath, false));
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index DIP file storage path", e);
    }

    setPermissions(dip.getPermissions(), doc);
    return doc;
  }

  private static List<String> getDIPFileAncestorsPath(String dipId, List<String> path) {
    List<String> parentFileDirectoryPath = new ArrayList<>();
    List<String> ancestorsPath = new ArrayList<>();
    parentFileDirectoryPath.addAll(path);

    while (!parentFileDirectoryPath.isEmpty()) {
      int lastElementIndex = parentFileDirectoryPath.size() - 1;
      String parentFileId = parentFileDirectoryPath.get(lastElementIndex);
      parentFileDirectoryPath.remove(lastElementIndex);
      ancestorsPath.add(0, IdUtils.getDIPFileId(dipId, parentFileDirectoryPath, parentFileId));
    }

    return ancestorsPath;
  }

  public static DIPFile solrDocumentToDIPFile(SolrDocument doc) {
    DIPFile file = new DIPFile();
    file.setUUID(objectToString(doc.get(RodaConstants.INDEX_UUID), null));
    file.setId(objectToString(doc.get(RodaConstants.DIPFILE_ID), null));
    file.setDipId(objectToString(doc.get(RodaConstants.DIPFILE_DIP_ID), null));
    file.setPath(objectToListString(doc.get(RodaConstants.DIPFILE_PATH)));
    file.setAncestorsUUIDs(objectToListString(doc.get(RodaConstants.DIPFILE_ANCESTORS_UUIDS)));
    file.setDirectory(objectToBoolean(doc.get(RodaConstants.DIPFILE_IS_DIRECTORY), Boolean.FALSE));
    file.setStoragePath(objectToString(doc.get(RodaConstants.DIPFILE_STORAGE_PATH), null));
    file.setSize(objectToLong(doc.get(RodaConstants.DIPFILE_SIZE), 0L));
    return file;
  }

  /*
   * Partial updates of RODA objects
   * ____________________________________________________________________________________________________________________
   */

  public static SolrInputDocument aipStateUpdateToSolrDocument(AIP aip) {
    return stateUpdateToSolrDocument(aip.getId(), aip.getState());
  }

  public static SolrInputDocument representationStateUpdateToSolrDocument(Representation representation,
    AIPState state) {
    return stateUpdateToSolrDocument(IdUtils.getRepresentationId(representation), state);
  }

  public static SolrInputDocument fileStateUpdateToSolrDocument(File file, AIPState state) {
    return stateUpdateToSolrDocument(IdUtils.getFileId(file), state);
  }

  public static SolrInputDocument preservationEventStateUpdateToSolrDocument(String preservationEventID,
    String preservationEventAipId, AIPState state) {
    SolrInputDocument document = stateUpdateToSolrDocument(preservationEventID, state);
    document.addField(RodaConstants.PRESERVATION_EVENT_AIP_ID, preservationEventAipId);
    return document;

  }

  private static SolrInputDocument stateUpdateToSolrDocument(String uuid, AIPState state) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, uuid);
    doc.addField(RodaConstants.STATE, set(state.toString()));
    return doc;
  }

  public static SolrInputDocument aipPermissionsUpdateToSolrDocument(AIP aip) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField(RodaConstants.INDEX_UUID, aip.getId());
    return permissionsUpdateToSolrDocument(document, aip.getPermissions());
  }

  public static SolrInputDocument dipPermissionsUpdateToSolrDocument(DIP dip) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField(RodaConstants.INDEX_UUID, dip.getId());
    return permissionsUpdateToSolrDocument(document, dip.getPermissions());
  }

  public static SolrInputDocument representationPermissionsUpdateToSolrDocument(Representation representation,
    Permissions permissions) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField(RodaConstants.INDEX_UUID, IdUtils.getRepresentationId(representation));
    return permissionsUpdateToSolrDocument(document, permissions);
  }

  public static SolrInputDocument filePermissionsUpdateToSolrDocument(File file, Permissions permissions) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField(RodaConstants.INDEX_UUID, IdUtils.getFileId(file));
    return permissionsUpdateToSolrDocument(document, permissions);
  }

  public static SolrInputDocument preservationEventPermissionsUpdateToSolrDocument(String preservationEventID,
    String preservationEventAipId, Permissions permissions, AIPState state) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField(RodaConstants.INDEX_UUID, preservationEventID);
    document = permissionsUpdateToSolrDocument(document, permissions);
    document.addField(RodaConstants.STATE, state.toString());
    document.addField(RodaConstants.PRESERVATION_EVENT_AIP_ID, preservationEventAipId);
    return document;
  }

  private static SolrInputDocument permissionsUpdateToSolrDocument(SolrInputDocument doc, Permissions permissions) {
    for (Entry<PermissionType, Set<String>> entry : permissions.getUsers().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      doc.addField(key, set(value));
    }

    for (Entry<PermissionType, Set<String>> entry : permissions.getGroups().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      doc.addField(key, set(value));
    }

    return doc;
  }

  public static SolrInputDocument updateAIPParentId(String aipId, String parentId, List<String> ancestors)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, aipId);
    doc.addField(RodaConstants.AIP_PARENT_ID, set(parentId));
    doc.addField(RodaConstants.AIP_ANCESTORS, set(ancestors));
    return doc;
  }

  public static SolrInputDocument updateAIPAncestors(String aipId, List<String> ancestors)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, aipId);
    doc.addField(RodaConstants.AIP_ANCESTORS, set(ancestors));
    return doc;
  }

  public static SolrInputDocument updateAIPHasRepresentations(String aipId, boolean hasRepresentations)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, aipId);
    doc.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, set(hasRepresentations));
    return doc;
  }

  public static SolrInputDocument updateRepresentationAncestors(String representationUUID, List<String> ancestors)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, representationUUID);
    doc.addField(RodaConstants.REPRESENTATION_ANCESTORS, set(ancestors));
    return doc;
  }

  public static SolrInputDocument updateFileAncestors(String fileUUID, List<String> ancestors)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, fileUUID);
    doc.addField(RodaConstants.FILE_ANCESTORS, set(ancestors));
    return doc;
  }

  public static Map<String, Object> set(Object value) {
    Map<String, Object> fieldModifier = new HashMap<>(1);
    // 20160511 this workaround fixes solr wrong behaviour with partial update
    // of empty lists
    if (value instanceof List && ((List<?>) value).isEmpty()) {
      fieldModifier.put("set", null);
    } else {
      fieldModifier.put("set", value);
    }
    return fieldModifier;
  }

  /*
   * Crosswalks auxiliary methods: RODA Objects <-> Apache Solr documents
   * ____________________________________________________________________________________________________________________
   */
  private static Permissions getPermissions(SolrDocument doc) {
    Permissions permissions = new Permissions();

    EnumMap<PermissionType, Set<String>> userPermissions = new EnumMap<>(PermissionType.class);

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + type;
      Set<String> users = new HashSet<>();
      users.addAll(objectToListString(doc.get(key)));
      userPermissions.put(type, users);
    }

    EnumMap<PermissionType, Set<String>> groupPermissions = new EnumMap<>(PermissionType.class);

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + type;
      Set<String> groups = new HashSet<>();
      groups.addAll(objectToListString(doc.get(key)));
      groupPermissions.put(type, groups);
    }

    permissions.setUsers(userPermissions);
    permissions.setGroups(groupPermissions);
    return permissions;
  }

  private static void setPermissions(Permissions permissions, final SolrInputDocument ret) {

    for (Entry<PermissionType, Set<String>> entry : permissions.getUsers().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      ret.addField(key, value);
    }

    for (Entry<PermissionType, Set<String>> entry : permissions.getGroups().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      ret.addField(key, value);
    }
  }

  public static List<String> getAncestors(String parentId, ModelService model)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    List<String> ancestors = new ArrayList<>();
    String nextAncestorId = parentId;
    while (nextAncestorId != null) {
      try {
        AIP nextAncestor = model.retrieveAIP(nextAncestorId);
        if (ancestors.contains(nextAncestorId)) {
          break;
        }
        ancestors.add(nextAncestorId);
        nextAncestorId = nextAncestor.getParentId();
      } catch (NotFoundException e) {
        LOGGER.warn("Could not find one AIP ancestor. Ancestor id: {}", nextAncestorId);
        nextAncestorId = null;
      }
    }
    return ancestors;
  }

  /**
   * WARNING: this should only be used to debug/tests only
   * 
   * @return
   * @throws IOException
   * @throws SolrServerException
   */
  public static QueryResponse executeSolrQuery(SolrClient index, String collection, String solrQueryString)
    throws SolrServerException, IOException {
    LOGGER.trace("query string: {}", solrQueryString);
    SolrQuery query = new SolrQuery();
    for (String string : solrQueryString.split("&")) {
      String[] split = string.split("=");
      query.add(split[0], split[1]);
    }
    LOGGER.trace("executeSolrQuery: {}", query);
    return index.query(collection, query);
  }

  public static SolrInputDocument premisToSolr(PreservationMetadataType preservationMetadataType, AIP aip,
    String representationUUID, String fileUUID, Binary binary) throws GenericException {
    SolrInputDocument doc;

    Map<String, String> stylesheetOpt = new HashMap<>();
    stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
      PreservationMetadataEventClass.REPOSITORY.toString());

    if (aip != null) {
      stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, PreservationMetadataEventClass.AIP.toString());
      stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId());

      if (representationUUID != null) {
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representationUUID);
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
          PreservationMetadataEventClass.REPRESENTATION.toString());
      }

      if (fileUUID != null) {
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_FILE_UUID, fileUUID);
        stylesheetOpt.put(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
          PreservationMetadataEventClass.FILE.toString());
      }
    }

    Reader reader = null;

    try {
      reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_CROSSWALKS_INGEST_OTHER,
        RodaConstants.PREMIS_METADATA_TYPE, RodaConstants.PREMIS_METADATA_VERSION, stylesheetOpt);

      XMLLoader loader = new XMLLoader();
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(reader);

      boolean parsing = true;
      doc = null;
      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT && "doc".equals(parser.getLocalName())) {
          doc = loader.readDoc(parser);
        }

      }

    } catch (XMLStreamException | FactoryConfigurationError e) {
      throw new GenericException("Could not process PREMIS " + binary.getStoragePath(), e);
    } finally {
      IOUtils.closeQuietly(reader);
    }

    if (preservationMetadataType == PreservationMetadataType.EVENT && doc != null) {
      try {
        List<LinkingIdentifier> agents = PremisV3Utils.extractAgentsFromEvent(binary);
        for (LinkingIdentifier id : agents) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER, JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking agent field: {}", e.getMessage());
      }
      try {
        List<LinkingIdentifier> sources = PremisV3Utils.extractObjectFromEvent(binary);
        for (LinkingIdentifier id : sources) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking source field: {}", e.getMessage());
      }
      try {
        List<LinkingIdentifier> outcomes = PremisV3Utils.extractObjectFromEvent(binary);
        for (LinkingIdentifier id : outcomes) {
          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(id));
        }
      } catch (org.roda.core.data.v2.validation.ValidationException e) {
        LOGGER.warn("Error setting linking outcome field: {}", e.getMessage());
      }

      // indexing active state and permissions
      if (aip != null) {
        doc.addField(RodaConstants.STATE, aip.getState().toString());
        setPermissions(aip.getPermissions(), doc);
      } else {
        doc.addField(RodaConstants.STATE, AIPState.ACTIVE);
      }
    }

    // set uuid from id defined in xslt
    if (doc != null) {
      doc.addField(RodaConstants.INDEX_UUID, doc.getFieldValue(RodaConstants.PRESERVATION_EVENT_ID));
    } else {
      doc = new SolrInputDocument();
    }

    return doc;
  }

  public static <T extends IsIndexed> List<String> suggest(SolrClient index, Class<T> classToRetrieve, String field,
    String queryString, boolean justActive, User user, boolean allowPartial) throws GenericException {
    StringBuilder queryBuilder = new StringBuilder();
    appendKeyValue(queryBuilder, field + RodaConstants.INDEX_SEARCH_SUFFIX, queryString + "*");
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setQuery(queryBuilder.toString());
    if (hasPermissionFilters(classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, justActive, classToRetrieve));
    }
    parseAndConfigureFacets(new Facets(new SimpleFacetParameter(field)), query);
    List<String> suggestions = new ArrayList<>();
    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve).get(0), query);
      response.getFacetField(field).getValues().forEach(count -> suggestions.add(count.getName()));
    } catch (SolrServerException | IOException | SolrException e) {
      throw new GenericException("Could not get suggestions", e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return suggestions;
  }

  public static <T extends IsIndexed> void execute(SolrClient index, Class<T> classToRetrieve, Filter filter,
    List<String> fieldsToReturn, IndexRunnable<T> indexRunnable, final Consumer<RODAException> exceptionHandler)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {

    User user = null;
    boolean justActive = false;
    boolean removeDuplicates = true;

    IterableIndexResult<T> iterableIndexResult = new IterableIndexResult<>(index, classToRetrieve, filter, Sorter.NONE,
      Facets.NONE, user, justActive, removeDuplicates, fieldsToReturn);

    if (iterableIndexResult.getTotalObjects() > 0) {
      iterableIndexResult.forEach(target -> {
        try {
          indexRunnable.run(target);
        } catch (RODAException e) {
          exceptionHandler.accept(e);
        }
      });
    }
  }

  private static SolrInputDocument solrDocumentToSolrInputDocument(SolrDocument d) {
    SolrInputDocument doc = new SolrInputDocument();
    for (String name : d.getFieldNames()) {
      doc.addField(name, d.getFieldValue(name));
    }
    return doc;
  }

  public static <T extends IsIndexed> void delete(SolrClient index, Class<T> classToDelete, List<String> ids)
    throws GenericException {
    try {
      index.deleteById(getIndexName(classToDelete).get(0), ids);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  public static <T extends IsIndexed> void delete(SolrClient index, Class<T> classToDelete, Filter filter)
    throws GenericException, RequestNotValidException {
    try {
      index.deleteByQuery(getIndexName(classToDelete).get(0), parseFilter(filter));
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  public static <T extends IsIndexed> void deleteByQuery(SolrClient index, String classToDelete, Filter filter)
    throws GenericException, RequestNotValidException {
    try {
      index.deleteByQuery(classToDelete, parseFilter(filter));
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }
}
