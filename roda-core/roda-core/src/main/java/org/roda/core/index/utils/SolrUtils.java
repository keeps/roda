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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DateGranularity;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.exceptions.SolrRetryException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.collapse.Collapse;
import org.roda.core.data.v2.index.collapse.HintEnum;
import org.roda.core.data.v2.index.collapse.NullPolicyEnum;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetParameter.SORT;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.ChildOfFilterParameter;
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
import org.roda.core.data.v2.index.filter.ParentWhichFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.HasPermissionFilters;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadata;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.Binary;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import dev.failsafe.Failsafe;
import dev.failsafe.Fallback;

/**
 * Utilities class related to Apache Solr
 *
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luís Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public class SolrUtils {
  public static final String COMMON = "common";
  public static final String CONF = "conf";
  public static final String SCHEMA = "managed-schema.xml";
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
    FindRequest findRequest = FindRequest.getBuilder(filter, justActive).build();
    return find(index, classToRetrieve, findRequest, user).getTotalCount();
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, Class<T> classToRetrieve, String id, User user,
    List<String> fieldsToReturn) throws NotFoundException, GenericException, AuthorizationDeniedException {
    T ret = retrieve(index, classToRetrieve, id, fieldsToReturn);
    UserUtility.checkObjectPermissions(user, ret, PermissionType.READ);
    return ret;
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, Class<T> classToRetrieve, String id,
    List<String> fieldsToReturn, boolean appendChildren) throws NotFoundException, GenericException {
    if (id == null) {
      throw new GenericException("Could not retrieve object from a null id");
    }

    T ret;
    SolrDocument doc;
    try {
      if (appendChildren) {
        Map<String, String> param = new HashMap<>();
        param.put("fl", "*, [child]");
        SolrParams params = new MapSolrParams(param);
        doc = index.getById(SolrCollectionRegistry.getIndexName(classToRetrieve), id, params);
      } else {
        doc = index.getById(SolrCollectionRegistry.getIndexName(classToRetrieve), id);
      }

      if (doc != null) {
        ret = SolrCollectionRegistry.fromSolrDocument(classToRetrieve, doc, fieldsToReturn);
      } else {
        throw new NotFoundException("Could not find document " + id);
      }
    } catch (SolrServerException | SolrException | IOException | NotSupportedException e) {
      throw new GenericException("Could not retrieve object from index", e);
    }
    return ret;
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, Class<T> classToRetrieve, String id,
    List<String> fieldsToReturn) throws NotFoundException, GenericException {
    return retrieve(index, classToRetrieve, id, fieldsToReturn, false);
  }

  public static <T extends IsIndexed> List<T> retrieve(SolrClient index, Class<T> classToRetrieve, List<String> id,
    List<String> fieldsToReturn) throws GenericException {
    List<T> ret = new ArrayList<>();
    try {
      int block = RodaConstants.DEFAULT_PAGINATION_VALUE;
      for (int i = 0; i < id.size(); i += block) {
        List<String> subList = id.subList(i, (i + block <= id.size() ? i + block : id.size()));
        SolrDocumentList docs = index.getById(SolrCollectionRegistry.getIndexName(classToRetrieve), subList);
        for (SolrDocument doc : docs) {
          ret.add(SolrCollectionRegistry.fromSolrDocument(classToRetrieve, doc, fieldsToReturn));
        }
      }
    } catch (SolrServerException | SolrException | IOException | NotSupportedException e) {
      throw new GenericException("Could not retrieve object from index", e);
    }
    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, sorter, sublist, null, fieldsToReturn);
  }

  private static <T extends IsIndexed> QueryResponse query(SolrClient index, Class<T> classToRetrieve, SolrQuery query)
    throws GenericException, RequestNotValidException {

    try {
      return index.query(SolrCollectionRegistry.getIndexName(classToRetrieve), query, METHOD.POST);
    } catch (SolrServerException | IOException | NotSupportedException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }
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
      QueryResponse response = query(index, classToRetrieve, query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets, fieldsToReturn);
    } catch (NotSupportedException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  /**
   * Find using cursors. Set initial cursor to
   * {@link CursorMarkParams#CURSOR_MARK_START}.
   *
   * @param index
   * @param classToRetrieve
   * @param filter
   * @param cursorMark
   * @param fieldsToReturn
   * @return
   * @throws GenericException
   * @throws RequestNotValidException
   */
  public static <T extends IsIndexed> Pair<IndexResult<T>, String> find(SolrClient index, Class<T> classToRetrieve,
    Filter filter, int pageSize, String cursorMark, User user, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {

    Pair<IndexResult<T>, String> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setQuery(parseFilter(filter));
    if (hasPermissionFilters(classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, justActive, classToRetrieve));
    }

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    query.setSorts(Arrays.asList(SortClause.asc(RodaConstants.INDEX_UUID)));

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
    }

    try {
      QueryResponse response = query(index, classToRetrieve, query);
      IndexResult<T> result = queryResponseToIndexResult(response, classToRetrieve, Facets.NONE, fieldsToReturn);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (NotSupportedException e) {
      throw new GenericException("Could not query index", e);
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

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve,
    FindRequest findRequest, User user) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setSorts(parseSorter(findRequest.getSorter()));
    query.setStart(findRequest.getSublist().getFirstElementIndex());
    query.setRows(findRequest.getSublist().getMaximumElementCount());
    query.setFields(parseFieldsToReturn(findRequest));
    parseAndConfigureFacets(findRequest.getFacets(), query);
    if (hasPermissionFilters(classToRetrieve) && !hasNestedDocumentsFilter(findRequest.getFilter(), classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, findRequest.isOnlyActive(), classToRetrieve));
    }

    if (hasNestedDocumentsFilter(findRequest.getFilter(), classToRetrieve)) {
      ChildOfFilterParameter childOfFilter = (ChildOfFilterParameter) findRequest.getFilter().getParameters()
        .getFirst();
      if (childOfFilter.getParentFilter() != null) {
        FilterParameter filterParameter = buildQueryPermissions(user);
        AndFiltersParameters andFiltersParameters = new AndFiltersParameters(
          List.of(childOfFilter.getParentFilter(), filterParameter));
        childOfFilter.setParentFilter(andFiltersParameters);
      } else {
        childOfFilter.setParentFilter(buildQueryPermissions(user));
      }
      query.setQuery(parseFilter(findRequest.getFilter()));
    } else {
      query.setQuery(parseFilter(findRequest.getFilter()));
    }

    if (findRequest.getCollapse() != null) {
      query.addFilterQuery(parseCollapse(findRequest.getCollapse()));
    }

    try {
      QueryResponse response = query(index, classToRetrieve, query);
      ret = queryResponseToIndexResult(response, classToRetrieve, findRequest.getFacets(),
        findRequest.getFieldsToReturn());
    } catch (NotSupportedException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  private static FilterParameter buildQueryPermissions(User user) {
    List<FilterParameter> filters = new ArrayList<>();

    filters
      .add(new SimpleFilterParameter(RodaConstants.INDEX_PERMISSION_USERS_PREFIX + PermissionType.READ, user.getId()));

    for (String group : user.getGroups()) {
      filters.add(new SimpleFilterParameter(RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + PermissionType.READ, group));
    }

    return new OrFiltersParameters(filters);
  }

  private static <T extends IsIndexed> boolean hasNestedDocumentsFilter(Filter filter, Class<T> classToRetrieve) {
    if (!IndexedAIP.class.isAssignableFrom(classToRetrieve)) {
      return false;
    }

    return filter.getParameters().stream().anyMatch(ChildOfFilterParameter.class::isInstance);
  }

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */
  private static String[] parseFieldsToReturn(FindRequest findRequest) {
    if (findRequest.getChildren()) {
      List<String> fieldsToReturn = new ArrayList<>(findRequest.getFieldsToReturn());

      fieldsToReturn.add(parseChildTransformer(findRequest));
      return fieldsToReturn.toArray(new String[0]);
    }

    return findRequest.getFieldsToReturn().toArray(new String[0]);
  }

  private static String parseChildTransformer(FindRequest findRequest) {
    String childrenLimit = String
      .valueOf(findRequest.getChildrenLimit() != null ? findRequest.getChildrenLimit() : 100);

    if (findRequest.getChildrenFieldsToReturn() != null && !findRequest.getChildrenFieldsToReturn().isEmpty()) {
      String childrenFl = findRequest.getChildrenFieldsToReturn().stream().filter(Objects::nonNull)
        .collect(Collectors.joining(", "));

      return String.format("[child fl=%s limit=%s]", childrenFl, childrenLimit);
    }

    return String.format("[child limit=%s]", childrenLimit);
  }

  private static String parseCollapse(Collapse collapse) {
    return "{" + "!collapse field=" + collapse.getField() + " " + parseCollapseNullPolicy(collapse.getNullPolicy())
      + " " + parseCollapseHint(collapse.getHint()) + " " + parseCollapseSorter(collapse.getSorter()) + "}";
  }

  private static String parseCollapseSorter(Sorter sorter) {
    // sort='numeric_field asc, score desc'
    StringBuilder sb = new StringBuilder();

    if (sorter != null && sorter.getParameters().length != 0) {
      sb.append("sort='");
      for (SortParameter sortParameter : sorter.getParameters()) {
        sb.append(sortParameter.getName()).append(" ").append(sortParameter.isDescending() ? ORDER.desc : ORDER.asc);
      }
      sb.append("'");
    }
    return sb.toString();
  }

  private static String parseCollapseHint(HintEnum hintEnum) {
    return "hint=" + hintEnum.toString();
  }

  private static String parseCollapseNullPolicy(NullPolicyEnum nullPolicyEnum) {
    return "nullPolicy=" + nullPolicyEnum.toString();
  }

  private static <T> boolean hasPermissionFilters(Class<T> resultClass) {
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
    return string.replaceAll("([+&|!(){}\\[\\-\\]\\^\\\\~:\"/])", "\\\\$1");
  }

  public static List<String> objectToListString(Object object) {
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

  public static Float objectToFloat(Object object) {
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

  public static Date parseDate(String date) throws ParseException {
    Date ret;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // TemporalAccessor temporal = DateTimeFormatter.ISO_INSTANT.parse(date);
      // Instant instant = Instant.from(temporal);
      // ret = Date.from(instant);

      // TODO change to the former only when GWT supports Instant

      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static Date parseDateWithMillis(String date) throws ParseException {
    Date ret;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // TemporalAccessor temporal = DateTimeFormatter.ISO_INSTANT.parse(date);
      // Instant instant = Instant.from(temporal);
      // ret = Date.from(instant);

      // TODO change to the former only when GWT supports Instant

      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_WITH_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static String formatDate(Date date) {
    String ret = null;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
      // TODO change to former only when GWT supports Instant
      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.format(date);
    }
    return ret;
  }

  public static String formatDateWithMillis(Date date) {
    String ret = null;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
      // TODO change to former only when GWT supports Instant
      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_WITH_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.format(date);
    }
    return ret;
  }

  public static Instant parseInstant(String instant) {
    return Instant.from((DateTimeFormatter.ISO_INSTANT.parse(instant)));
  }

  public static String formatInstant(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant);
  }

  public static Instant dateToInstant(Date date) {
    return date != null ? date.toInstant() : null;
  }

  public static Date objectToDate(Object object) {
    Date ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Date) {
      ret = (Date) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseDate((String) object);
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

  public static Date objectToDateWithMillis(Object object) {
    Date ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Date) {
      ret = (Date) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseDateWithMillis((String) object);
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

  public static Instant objectToInstant(Object object) {
    Instant ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Instant) {
      ret = (Instant) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseInstant((String) object);
      } catch (DateTimeParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  public static Boolean objectToBoolean(Object object, Boolean defaultValue) {
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

  public static String objectToString(Object object, String defaultValue) {
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

  public static <E extends Enum<E>> String formatEnum(E enumValue) {
    return enumValue.name();
  }

  public static <E extends Enum<E>> E parseEnum(Class<E> enumeration, String enumValue) {
    return Enum.valueOf(enumeration, enumValue);
  }

  public static List<RepresentationInformationRelation> objectToRepresentationInformationRelation(Object object) {
    List<RepresentationInformationRelation> relations = new ArrayList<>();

    if (object == null) {
      return relations;
    }

    if (object instanceof SolrDocument doc) {
      RepresentationInformationRelation relation = new RepresentationInformationRelation();
      relation.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_TITLE), null));
      relation.setRelationType(
        SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_RELATION_TYPE), null));
      relation.setLink(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_LINK), null));
      relation.setObjectType(SolrUtils.objectToEnum(doc.get(RodaConstants.REPRESENTATION_INFORMATION_OBJECT_TYPE),
        RelationObjectType.class, null));
      relations.add(relation);
    } else {
      List<SolrDocument> documents = (List<SolrDocument>) object;
      documents.forEach(doc -> {
        RepresentationInformationRelation relation = new RepresentationInformationRelation();
        relation.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_TITLE), null));
        relation.setRelationType(
          SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_RELATION_TYPE), null));
        relation.setLink(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_LINK), null));
        relation.setObjectType(SolrUtils.objectToEnum(doc.get(RodaConstants.REPRESENTATION_INFORMATION_OBJECT_TYPE),
          RelationObjectType.class, null));
        relations.add(relation);
      });
    }

    return relations;
  }

  public static <E extends Enum<E>> E objectToEnum(Object object, Class<E> enumeration, E defaultValue) {
    E ret = defaultValue;
    if (object != null) {
      if (object instanceof String name) {
        try {
          ret = parseEnum(enumeration, name);
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

  private static <T extends IsIndexed> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    Class<T> responseClass, Facets facets, List<String> liteFields) throws GenericException, NotSupportedException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<>();

    for (SolrDocument doc : docList) {
      docs.add(SolrCollectionRegistry.fromSolrDocument(responseClass, doc, liteFields));
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

    try (Reader transformationResult = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_CROSSWALKS_INGEST,
      metadataType, metadataVersion, parameters)) {

      SolrXMLLoader loader = new SolrXMLLoader();
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

    } catch (XMLStreamException | FactoryConfigurationError | IOException e) {
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath(), e);
    }

    return doc == null ? new SolrInputDocument() : validateDescriptiveMetadataFields(doc);
  }

  public static SolrInputDocument getTechnicalMetadataFields(Binary binary, String metadataType, String metadataVersion)
    throws GenericException {
    SolrInputDocument doc;

    Map<String, String> parameters = new HashMap<>();
    parameters.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);

    String technicalMetadataStylesheetName = getTechnicalMetadataStylesheetName(metadataType, metadataVersion);

    if ((RodaCoreFactory.getConfigurationFileAsStream(technicalMetadataStylesheetName)) == null) {
      return new SolrInputDocument();
    }

    try (Reader transformationResult = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_CROSSWALKS_TECHNICAL,
      metadataType, metadataVersion, parameters)) {

      if (!transformationResult.ready()) {
        return new SolrInputDocument();
      }

      SolrXMLLoader loader = new SolrXMLLoader();
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

    } catch (XMLStreamException | FactoryConfigurationError | IOException e) {
      throw new GenericException("Could not process technical metadata binary " + binary.getStoragePath(), e);
    }

    return doc == null ? new SolrInputDocument() : validateDescriptiveMetadataFields(doc);
  }

  private static String getTechnicalMetadataStylesheetName(String type, String version) {
    if (StringUtils.isBlank(version)) {
      return RodaConstants.CORE_CROSSWALKS_TECHNICAL + type.toLowerCase() + ".xslt";
    }

    return RodaConstants.CORE_CROSSWALKS_TECHNICAL + type.toLowerCase() + RodaConstants.METADATA_VERSION_SEPARATOR
      + version + ".xslt";
  }

  private static SolrInputDocument validateDescriptiveMetadataFields(SolrInputDocument doc) {
    if (doc.get(RodaConstants.AIP_DATE_INITIAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_INITIAL).getValue();
      if (value instanceof String stringDate) {
        try {
          Date d = parseDate(stringDate);
          doc.setField(RodaConstants.AIP_DATE_INITIAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_INITIAL);
          doc.setField(RodaConstants.AIP_DATE_INITIAL + "_txt", value);
        }
      }
    }

    if (doc.get(RodaConstants.AIP_DATE_FINAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_FINAL).getValue();
      if (value instanceof String stringDate) {
        try {
          Date d = parseDate(stringDate);
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
      return ret.toString();
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        parseFilterParameter(ret, parameter, true);
      }
    }

    LOGGER.trace("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    if (parameter instanceof SimpleFilterParameter simplePar) {
      appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OneOfManyFilterParameter param) {
      appendValuesUsingOROperator(ret, param.getName(), param.getValues(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof BasicSearchFilterParameter param) {
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof EmptyKeyFilterParameter param) {
      appendANDOperator(ret, true);
      ret.append("(*:* NOT ").append(param.getName()).append(":*)");
    } else if (parameter instanceof DateRangeFilterParameter param) {
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
        processToDate(param.getToValue(), param.getGranularity(), false), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof DateIntervalFilterParameter param) {
      appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
        param.getGranularity(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof LongRangeFilterParameter param) {
      appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof NotSimpleFilterParameter notSimplePar) {
      appendNotExactMatch(ret, notSimplePar.getName(), notSimplePar.getValue(), true,
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OrFiltersParameters || parameter instanceof AndFiltersParameters) {
      FiltersParameters filters = (FiltersParameters) parameter;
      appendFiltersWithOperator(ret, parameter instanceof OrFiltersParameters ? "OR" : "AND", filters.getValues(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof AllFilterParameter) {
      appendSelectAll(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof ParentWhichFilterParameter nestParentFilterParameter) {
      appendBlockJoinFilterParameter(ret, nestParentFilterParameter, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof ChildOfFilterParameter nestChildOfFilterParameter) {
      appendBlockJoinChildrenFilterParameter(ret, nestChildOfFilterParameter, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      LOGGER.error("Unsupported filter parameter class: {}", parameter.getClass().getName());
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
    }
  }

  private static void appendBlockJoinChildrenFilterParameter(StringBuilder ret, ChildOfFilterParameter parameter,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    StringBuilder blockMask = new StringBuilder();
    parseFilterParameter(blockMask, parameter.getChildrenOfFilter(), prefixWithANDOperatorIfBuilderNotEmpty);
    String replace = blockMask.toString().replace(": ", ":");

    if (parameter.getParentFilter() != null) {
      StringBuilder someParents = new StringBuilder();
      parseFilterParameter(someParents, parameter.getParentFilter(), prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("{!child of=").append(replace).append("} ").append(someParents);
    } else {
      ret.append("{!child of=").append(replace).append("}");
    }
  }

  private static void appendBlockJoinFilterParameter(StringBuilder ret, ParentWhichFilterParameter parameter,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    StringBuilder blockMask = new StringBuilder();
    parseFilterParameter(blockMask, parameter.getParentFilter(), prefixWithANDOperatorIfBuilderNotEmpty);
    String replace = blockMask.toString().replace(": ", ":");

    if (parameter.getChildrenFilter() != null) {
      StringBuilder someChildren = new StringBuilder();
      parseFilterParameter(someChildren, parameter.getChildrenFilter(), prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("{!parent which=").append(replace).append("} ").append(someChildren);
    } else {
      ret.append("{!parent which=").append(replace).append("}");
    }
  }

  private static void appendSelectAll(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty) {
      ret.append("*:*");
    }
  }

  private static void appendANDOperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && !ret.isEmpty()) {
      ret.append(" AND ");
    }
  }

  private static void appendOROperator(StringBuilder ret, boolean prefixWithOROperatorIfBuilderNotEmpty) {
    if (prefixWithOROperatorIfBuilderNotEmpty && !ret.isEmpty()) {
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
        ret.append(" ").append(operator).append(" ");
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
        String date = formatDate((Date) value);
        LOGGER.trace("Appending date value \"{}\" to range", date);
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append(value);
      } else if (valueClass.equals(String.class)) {
        ret.append((String) value);
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
      return formatDate(fromValue);
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
      Date toValueWithoutTimeZone = (Date) toValue.clone();
      sb.append(formatDate(toValueWithoutTimeZone));

      switch (granularity) {
        case YEAR -> sb.append("+1YEAR-1MILLISECOND");
        case MONTH -> sb.append("+1MONTH-1MILLISECOND");
        case DAY -> sb.append("+1DAY-1MILLISECOND");
        case HOUR -> sb.append("+1HOUR-1MILLISECOND");
        case MINUTE -> sb.append("+1MINUTE-1MILLISECOND");
        case SECOND -> sb.append("+1SECOND-1MILLISECOND");
        default -> {
          // do nothing
        }
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

        if (facetParameter instanceof SimpleFacetParameter simpleFacetParameter) {
          setQueryFacetParameter(query, simpleFacetParameter);
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(), facetParameter.getValues(), true);
        } else {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        }
      }
      if (!filterQuery.isEmpty()) {
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

    StringBuilder fq = getFilterQueryPermissions(user);

    if (justActive && SolrCollection.hasStateFilter(classToRetrieve)) {
      appendExactMatch(fq, RodaConstants.INDEX_STATE, SolrUtils.formatEnum(AIPState.ACTIVE), true, true);
    }

    return fq.toString();
  }

  private static StringBuilder getFilterQueryPermissions(User user) {
    StringBuilder ret = new StringBuilder();

    // TODO find a better way to define admin super powers
    if (user != null && !RodaConstants.ADMIN.equals(user.getName())) {
      ret.append("(");
      String usersKey = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + PermissionType.READ;
      appendExactMatch(ret, usersKey, user.getId(), true, false);

      String groupsKey = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + PermissionType.READ;
      appendValuesUsingOROperatorForQuery(ret, groupsKey, new ArrayList<>(user.getGroups()), true);

      ret.append(")");
    }
    return ret;
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

    Fallback<Object> fallback = Fallback.of(e -> {
    });

    for (String collection : collections) {
      Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy())
        .onFailure(e -> LOGGER.error("Error committing into collection: {}", collection, e.getException()))
        .run(() -> index.commit(collection, waitFlush, waitSearcher, softCommit));
    }
  }

  public static void commit(SolrClient index, List<Class<? extends IsIndexed>> resultClasses) throws GenericException {
    List<String> collections = new ArrayList<>();
    for (Class<? extends IsIndexed> resultClass : resultClasses) {
      try {
        collections.add(SolrCollectionRegistry.getIndexName(resultClass));
      } catch (NotSupportedException e) {
        throw new GenericException(e);
      }
    }

    commit(index, collections.toArray(new String[] {}));
  }

  @SafeVarargs
  public static void commit(SolrClient index, Class<? extends IsIndexed>... resultClasses) throws GenericException {
    commit(index, Arrays.asList(resultClasses));
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> create(SolrClient index,
    String classToCreate, SolrInputDocument instance, S source) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>(source);

    Fallback<Object> fallback = Fallback.of(e -> {
      ret.add(new SolrRetryException(e.getLastException()));
    });

    if (instance != null) {
      Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy()).onFailure(e -> {
        LOGGER.error("Error adding document to index", e.getException());
      }).run(() -> index.add(classToCreate, instance));
    }

    return ret;
  }

  public static <I extends IsIndexed, M extends IsModelObject, S extends Object> ReturnWithExceptions<Void, S> create2(
    SolrClient index, ModelService model, S source, Class<I> indexClass, M object, IndexingAdditionalInfo utils) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>(source);

    Fallback<Object> fallback = Fallback.of(e -> {
      ret.add(new SolrRetryException(e.getLastException()));
    });

    if (object != null) {
      try {
        SolrInputDocument solrDocument = SolrCollectionRegistry.toSolrDocument(indexClass, model, object, utils);
        if (solrDocument != null) {
          Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy())
            .onFailure(e -> LOGGER.error("Error adding document to index", e.getException()))
            .run(() -> index.add(SolrCollectionRegistry.getIndexName(indexClass), solrDocument));
        }
      } catch (GenericException | NotSupportedException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.error("Error adding document to index", e);
        ret.add(e);
      }
    }

    return ret;
  }

  public static <I extends IsIndexed, M extends IsModelObject, S extends Object> ReturnWithExceptions<Void, S> create2(
    SolrClient index,  ModelService model, S source, Class<I> indexClass, M object) {
    return create2(index, model, source, indexClass, object, IndexingAdditionalInfo.empty());
  }

  public static <T extends IsIndexed, M extends IsModelObject, S extends Object> ReturnWithExceptions<Void, S> create(
    SolrClient index, ModelService model, Class<T> classToCreate, M instance, S source) {
    return create(index, model, classToCreate, instance, source, false);
  }

  public static <T extends IsIndexed, M extends IsModelObject, S extends Object> ReturnWithExceptions<Void, S> create(
    SolrClient index, ModelService model, Class<T> classToCreate, M instance, S source, boolean commit) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>();
    try {
      Optional<SolrInputDocument> solrDocument = Optional
        .of(SolrCollectionRegistry.toSolrDocument(classToCreate, model, instance));
      if (solrDocument.isPresent()) {
        create(index, SolrCollectionRegistry.getIndexName(classToCreate), solrDocument.get(), source).addTo(ret);
        if (commit) {
          commit(index, classToCreate);
        }
      }
    } catch (NotSupportedException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Error adding document to index", e);
      ret.add(e);
    }

    return ret;
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> update(SolrClient index,
    Class<T> classToCreate, String uuid, Map<String, Object> fields, S source) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>();
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.INDEX_UUID, uuid);
    fields.forEach((key, value) -> doc.addField(key, set(value)));
    try {
      create(index, SolrCollectionRegistry.getIndexName(classToCreate), doc, source).addTo(ret);
    } catch (NotSupportedException e) {
      LOGGER.error("Error adding document to index", e);
      ret.add(e);
    }
    return ret;
  }

  private static Map<String, Object> set(Object value) {
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
   * Common mappings of RODA objects
   * ____________________________________________________________________________________________________________________
   */

  public static void indexRepresentationTechnicalMetadata(ModelService model,
    List<TechnicalMetadata> technicalMetadatum, String fileId, SolrInputDocument doc)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {

    // guarding against repeated fields
    Set<String> usedNonRepeatableFields = new HashSet<>();

    // technical metadata ids
    ArrayList<String> techMdIds = new ArrayList<>();

    for (TechnicalMetadata techMd : technicalMetadatum) {
      techMdIds.add(techMd.getType().toLowerCase());

      String urn = URNUtils.createRodaTechnicalMetadataURN(fileId, RODAInstanceUtils.getLocalInstanceIdentifier(),
        techMd.getType().toLowerCase());

      StoragePath storagePath = ModelUtils.getTechnicalMetadataStoragePath(techMd.getAipId(),
        techMd.getRepresentationId(), Collections.singletonList(techMd.getType()),
        urn + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION);

      Binary binary = model.getStorage().getBinary(storagePath);

      try {
        SolrInputDocument technicalMetadataFields = getTechnicalMetadataFields(binary, techMd.getType(), null);
        for (SolrInputField field : technicalMetadataFields) {
          if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
            boolean added = usedNonRepeatableFields.add(field.getName());
            if (added) {
              doc.addField(field.getName(), field.getValue());
            }
          } else {
            doc.addField(field.getName(), field.getValue());
          }
        }
      } catch (GenericException e) {
        LOGGER.warn("Problem processing technical metadata: {}", e.getMessage(), e);
      } catch (Exception e) {
        LOGGER.error("Error processing technical metadata: {}", techMd, e);
      }
    }

    doc.addField(RodaConstants.FILE_TECHNICAL_METADATA_ID, techMdIds);
  }

  public static void indexDescriptiveMetadataFields(ModelService model, String aipId, String representationId,
    List<DescriptiveMetadata> metadataList, SolrInputDocument doc)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // guarding against repeated fields
    Set<String> usedNonRepeatableFields = new HashSet<>();

    for (DescriptiveMetadata metadata : metadataList) {
      Binary binary = model.getBinary(metadata);
      try {
        SolrInputDocument fields = getDescriptiveMetadataFields(binary, metadata.getType(), metadata.getVersion());
        for (SolrInputField field : fields) {
          if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
            boolean added = usedNonRepeatableFields.add(field.getName());
            if (added) {
              doc.addField(field.getName(), field.getValue());
            }
          } else {
            doc.addField(field.getName(), field.getValue());
          }
        }
      } catch (GenericException e) {
        LOGGER.warn("Problem processing descriptive metadata: {}", e.getMessage(), e);
      } catch (Exception e) {
        LOGGER.error("Error processing descriptive metadata: {}", metadata, e);
      }
    }
  }

  public static List<String> getFileAncestorsPath(String aipId, String representationId, List<String> path) {
    List<String> ancestorsPath = new ArrayList<>();

    List<String> parentFileDirectoryPath = new ArrayList<>(path);

    while (!parentFileDirectoryPath.isEmpty()) {
      int lastElementIndex = parentFileDirectoryPath.size() - 1;
      String parentFileId = parentFileDirectoryPath.get(lastElementIndex);
      parentFileDirectoryPath.remove(lastElementIndex);
      ancestorsPath.add(0, IdUtils.getFileId(aipId, representationId, parentFileDirectoryPath, parentFileId));
    }

    return ancestorsPath;
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

    for (Entry<String, List<String>> entry : otherProperties.entrySet()) {
      solrDocument.setField(prefix + entry.getKey(), entry.getValue());
    }

    return solrDocumentToSolrInputDocument(solrDocument);
  }

  public static <T extends IsIndexed> String getObjectLabel(SolrClient index, String className, String id) {
    // TODO move this logic to Solr Collections

    Fallback<Object> fallback = Fallback.of(e -> null);

    if (StringUtils.isNotBlank(className) && StringUtils.isNotBlank(id)) {
      try {
        Class<T> objectClass = (Class<T>) Class.forName(className);
        if (objectClass.equals(LiteRODAObject.class)) {
          return null;
        }
        String field = SolrCollectionRegistry.getIndexName(objectClass);

        SolrDocument doc = Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy()).onFailure(e -> {
          LOGGER.warn("Using the fallback strategy");
          e.getResult();
        }).get(() -> index.getById(field, id));

        if (doc != null) {
          if (objectClass.equals(AIP.class)) {
            return objectToString(doc.get(RodaConstants.AIP_TITLE), null);
          } else if (objectClass.equals(Representation.class)) {
            return objectToString(doc.get(RodaConstants.REPRESENTATION_ID), null);
          } else if (objectClass.equals(File.class)) {
            return objectToString(doc.get(RodaConstants.INDEX_ID), null);
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
      } catch (ClassNotFoundException | NotSupportedException e) {
        LOGGER.error("Could not return object label of {} {}", className, id, e);
      }
    }

    return null;
  }

  public static Permissions getPermissions(SolrDocument doc) {

    Permissions permissions = new Permissions();

    EnumMap<PermissionType, Set<String>> userPermissions = new EnumMap<>(PermissionType.class);

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + type;
      Set<String> users = new HashSet<>(objectToListString(doc.get(key)));
      userPermissions.put(type, users);
    }

    EnumMap<PermissionType, Set<String>> groupPermissions = new EnumMap<>(PermissionType.class);

    for (PermissionType type : PermissionType.values()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + type;
      Set<String> groups = new HashSet<>(objectToListString(doc.get(key)));
      groupPermissions.put(type, groups);
    }

    permissions.setUsers(userPermissions);
    permissions.setGroups(groupPermissions);
    return permissions;
  }

  public static void setPermissions(Permissions permissions, final SolrInputDocument ret) {

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

  public static Map<String, Object> getPermissionsAsPreCalculatedFields(Permissions permissions) {

    Map<String, Object> ret = new HashMap<>();

    for (Entry<PermissionType, Set<String>> entry : permissions.getUsers().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      ret.put(key, value);
    }

    for (Entry<PermissionType, Set<String>> entry : permissions.getGroups().entrySet()) {
      String key = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + entry.getKey();
      List<String> value = new ArrayList<>(entry.getValue());
      ret.put(key, value);
    }
    return ret;
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
      QueryResponse response = index.query(SolrCollectionRegistry.getIndexName(classToRetrieve), query);
      response.getFacetField(field).getValues().forEach(count -> suggestions.add(count.getName()));
    } catch (SolrServerException | IOException | SolrException | NotSupportedException e) {
      throw new GenericException("Could not get suggestions", e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return suggestions;
  }

  public static <T extends IsIndexed> void execute(SolrClient index, Class<T> classToRetrieve, Filter filter,
    List<String> fieldsToReturn, IndexRunnable<T> indexRunnable, final Consumer<RODAException> exceptionHandler)
    throws GenericException {

    try (IterableIndexResult<T> iterableIndexResult = new IterableIndexResult<>(index, classToRetrieve, filter, null,
      false, fieldsToReturn)) {

      iterableIndexResult.forEach(target -> {
        try {
          indexRunnable.run(target);
        } catch (RODAException e) {
          exceptionHandler.accept(e);
        }
      });

    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  private static SolrInputDocument solrDocumentToSolrInputDocument(SolrDocument d) {
    SolrInputDocument doc = new SolrInputDocument();
    for (String name : d.getFieldNames()) {
      doc.addField(name, d.getFieldValue(name));
    }
    return doc;
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> delete(SolrClient index,
    Class<T> classToDelete, List<String> ids, S source) {
    return delete(index, classToDelete, ids, source, false);
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> delete(SolrClient index,
    Class<T> classToDelete, List<String> ids, S source, boolean commit) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>();

    Fallback<Object> fallback = Fallback.of(e -> {
      ret.add(new SolrRetryException(e.getLastException()));
    });

    Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy())
      .onFailure(e -> LOGGER.error("Error deleting document from index")).run(() -> {
        index.deleteById(SolrCollectionRegistry.getIndexName(classToDelete), ids);
        if (commit) {
          commit(index, classToDelete);
        }
      });

    return ret;
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> delete(SolrClient index,
    Class<T> classToDelete, Filter filter, S source) {
    return delete(index, classToDelete, filter, source, false);
  }

  public static <T extends IsIndexed, S extends Object> ReturnWithExceptions<Void, S> delete(SolrClient index,
    Class<T> classToDelete, Filter filter, S source, boolean commit) {
    ReturnWithExceptions<Void, S> ret = new ReturnWithExceptions<>();

    Fallback<Object> fallback = Fallback.of(e -> {
      ret.add(new SolrRetryException(e.getLastException()));
    });

    Failsafe.with(fallback, RetryPolicyBuilder.getInstance().getRetryPolicy())
      .onFailure(e -> LOGGER.error("Error deleting documents from index")).run(() -> {
        index.deleteByQuery(SolrCollectionRegistry.getIndexName(classToDelete), parseFilter(filter));
        if (commit) {
          commit(index, classToDelete);
        }
      });

    return ret;
  }

  public static <T extends IsIndexed> void deleteByQuery(SolrClient index, String classToDelete, Filter filter)
    throws GenericException, RequestNotValidException {
    try {
      index.deleteByQuery(classToDelete, parseFilter(filter));
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  public static Map<String, String> getRetentionPeriod(DisposalSchedule disposalSchedule, AIP aip)
    throws NotFoundException, GenericException {
    Map<String, String> values = new HashMap<>();

    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(disposalSchedule.getActionCode())) {
      return values;
    }

    Date overDueDate;
    Integer retentionPeriodDuration = disposalSchedule.getRetentionPeriodDuration();
    Calendar cal = Calendar.getInstance();

    IndexedAIP retrieve = retrieve(RodaCoreFactory.getSolr(), IndexedAIP.class, aip.getId(),
      Collections.singletonList(disposalSchedule.getRetentionTriggerElementId()));

    Object o = retrieve.getFields().get(disposalSchedule.getRetentionTriggerElementId());

    if (o == null) {
      values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DETAILS, "Retention period start date is missing");
      values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION, RetentionPeriodCalculation.ERROR.name());
      return values;
    }

    if (o instanceof Date retentionPeriodStartDate) {
      cal.setTime(retentionPeriodStartDate);
    } else {
      values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DETAILS, "Retention period start must be of date type");
      values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION, RetentionPeriodCalculation.ERROR.name());
      return values;
    }

    if (!disposalSchedule.getRetentionPeriodIntervalCode().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      switch (disposalSchedule.getRetentionPeriodIntervalCode()) {
        case YEARS:
          cal.add(Calendar.YEAR, retentionPeriodDuration);
          break;
        case MONTHS:
          cal.add(Calendar.MONTH, retentionPeriodDuration);
          break;
        case WEEKS:
          cal.add(Calendar.WEEK_OF_MONTH, retentionPeriodDuration);
          break;
        case DAYS:
          cal.add(Calendar.DATE, retentionPeriodDuration);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + disposalSchedule.getRetentionPeriodIntervalCode());
      }
    }
    overDueDate = cal.getTime();

    values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_START_DATE, formatDate(objectToDate(o)));
    values.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION, RetentionPeriodCalculation.SUCCESS.name());
    values.put(RodaConstants.AIP_OVERDUE_DATE, formatDate(overDueDate));

    return values;
  }

  public static DisposalSchedule getDisposalSchedule(AIP aip, ModelService model)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    DisposalSchedule disposalSchedule;
    try {
      if (StringUtils.isNoneBlank(aip.getDisposalScheduleId())) {
        disposalSchedule = model.retrieveDisposalSchedule(aip.getDisposalScheduleId());
      } else {
        return null;
      }
    } catch (NotFoundException e) {
      return null;
    }

    return disposalSchedule;
  }
}
