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
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

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
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.handler.loader.XMLLoader;
import org.roda.common.RodaUtils;
import org.roda.index.IndexServiceException;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants.DateGranularity;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.adapter.facet.FacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.facet.RangeFacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.facet.SimpleFacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LongRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RODAObject;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.data.v2.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.data.v2.SIPStateTransition;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleEventPreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationFilePreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationPreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * Utilities class related to Apache Solr
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luís Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public class SolrUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);
  private static final String ID_SEPARATOR = ".";

  /** Private empty constructor */
  private SolrUtils() {

  }

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
    Class<T> responseClass, Facets facets) throws IndexServiceException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<T>();

    for (SolrDocument doc : docList) {
      T result = solrDocumentTo(responseClass, doc);
      docs.add(result);
    }

    return new IndexResult<T>(offset, limit, totalCount, docs, facetResults);
  }

  private static List<FacetFieldResult> processFacetFields(Facets facets, List<FacetField> facetFields) {
    List<FacetFieldResult> ret = new ArrayList<FacetFieldResult>();
    FacetFieldResult facetResult;
    if (facetFields != null) {
      for (FacetField facet : facetFields) {
        LOGGER.trace("facet:" + facet.getName() + " count:" + facet.getValueCount());
        facetResult = new FacetFieldResult(facet.getName(), facet.getValueCount(),
          facets.getParameters().get(facet.getName()).getValues());
        for (Count count : facet.getValues()) {
          LOGGER.trace("   value:" + count.getName() + " value:" + count.getCount());
          facetResult.addFacetValue(count.getName(), count.getName(), count.getCount());
        }
        ret.add(facetResult);
      }
    }
    return ret;

  }

  public static SolrInputDocument getDescriptiveMetataFields(Binary binary) throws IndexServiceException {
    SolrInputDocument doc;
    InputStream inputStream;
    String xsltFilename = null;
    try {
      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);
      xsltFilename = binary.getStoragePath().getName() + ".xslt";

      ClassLoader classLoader = SolrUtils.class.getClassLoader();
      InputStream transformerStream = classLoader.getResourceAsStream(xsltFilename);

      if (transformerStream == null) {
        // use default
        xsltFilename = "plain.xslt";
        transformerStream = classLoader.getResourceAsStream(xsltFilename);
      }

      // TODO support the use of scripts for non-xml transformers

      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, String> stylesheetOpt = new HashMap<String, String>();
      stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
      RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
      descMetadataReader.close();

      XMLLoader loader = new XMLLoader();
      LOGGER.trace("Transformed desc. metadata:\n" + transformerResult);
      CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
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
      transformationResult.close();

    } catch (IOException | TransformerException | XMLStreamException | FactoryConfigurationError e) {
      throw new IndexServiceException(
        "Could not process descriptive metadata binary " + binary.getStoragePath() + " using xslt " + xsltFilename,
        IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return doc;
  }

  public static String parseFilter(Filter filter) throws IndexServiceException {
    StringBuilder ret = new StringBuilder();

    if (filter == null || filter.getParameters().isEmpty()) {
      ret.append("*:*");
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        if (parameter instanceof SimpleFilterParameter) {
          SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
          appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, true);
        } else if (parameter instanceof OneOfManyFilterParameter) {
          OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
          appendValuesUsingOROperator(ret, param.getName(), param.getValues());
        } else if (parameter instanceof BasicSearchFilterParameter) {
          BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
          appendBasicSearch(ret, param.getName(), param.getValue(), "AND", true);
        } else if (parameter instanceof EmptyKeyFilterParameter) {
          EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
          appendANDOperator(ret, true);
          ret.append("(*:* NOT " + param.getName() + ":*)");
        } else if (parameter instanceof DateRangeFilterParameter) {
          DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
          appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
            processToDate(param.getToValue(), param.getGranularity(), false));
        } else if (parameter instanceof DateIntervalFilterParameter) {
          DateIntervalFilterParameter param = (DateIntervalFilterParameter) parameter;
          appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
            param.getGranularity());
        } else if (parameter instanceof LongRangeFilterParameter) {
          LongRangeFilterParameter param = (LongRangeFilterParameter) parameter;
          appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue());
        } else {
          LOGGER.error("Unsupported filter parameter class: " + parameter.getClass().getName());
          throw new IndexServiceException("Unsupported filter parameter class: " + parameter.getClass().getName(),
            IndexServiceException.BAD_REQUEST);
        }
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.debug("Converting filter {} to query {}", filter, ret);
    return ret.toString();
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

  private static String processToDate(Date toValue, DateGranularity granularity, boolean returnStartOnNull) {
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
      ret = returnStartOnNull ? "*" : null;
    }
    return ret;
  }

  private static void appendRangeInterval(StringBuilder ret, String fromKey, String toKey, Date fromValue, Date toValue,
    DateGranularity granularity) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);
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

  private static <T extends Serializable, T1 extends Serializable> void appendRange(StringBuilder ret, String key,
    Class<T> fromClass, T fromValue, Class<T1> toClass, T1 toValue) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);

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
        LOGGER.debug("Appending date value \"" + date + "\" to range");
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append(Long.class.cast(value));
      } else if (valueClass.equals(String.class)) {
        ret.append(String.class.cast(value));
      } else {
        LOGGER.error("Cannot process range of the type " + valueClass);
      }
    } else {
      ret.append("*");
    }
  }

  public static List<SortClause> parseSorter(Sorter sorter) throws IndexServiceException {
    List<SortClause> ret = new ArrayList<SortClause>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SortClause(sortParameter.getName(), sortParameter.isDescending() ? ORDER.desc : ORDER.asc));
      }
    }
    return ret;
  }

  private static void parseAndConfigureFacets(Facets facets, SolrQuery query) throws IndexServiceException {
    if (facets != null) {
      query.setFacetSort(FacetParams.FACET_SORT_INDEX);
      if (!"".equals(facets.getQuery())) {
        query.addFacetQuery(facets.getQuery());
      }
      StringBuilder filterQuery = new StringBuilder();
      for (Entry<String, FacetParameter> parameter : facets.getParameters().entrySet()) {
        FacetParameter facetParameter = parameter.getValue();

        if (facetParameter instanceof SimpleFacetParameter) {
          setQueryFacetParameter(query, (SimpleFacetParameter) facetParameter);
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(),
            ((SimpleFacetParameter) facetParameter).getValues());
        } else if (facetParameter instanceof RangeFacetParameter) {
          LOGGER.error("Unsupported facet parameter class: " + facetParameter.getClass().getName());
        } else {
          LOGGER.error("Unsupported facet parameter class: " + facetParameter.getClass().getName());
        }
      }
      if (filterQuery.length() > 0) {
        query.addFilterQuery(filterQuery.toString());
        LOGGER.debug("Query after defining facets: " + query.toString());
      }
    }
  }

  private static void setQueryFacetParameter(SolrQuery query, SimpleFacetParameter facetParameter) {
    query.addFacetField(facetParameter.getName());
    if (facetParameter.getMinCount() != FacetParameter.DEFAULT_MIN_COUNT) {
      query.add(String.format("f.%s.facet.mincount", facetParameter.getName()),
        String.valueOf(facetParameter.getMinCount()));
    }
    if (facetParameter.getLimit() != SimpleFacetParameter.DEFAULT_LIMIT) {
      query.add(String.format("f.%s.facet.limit", facetParameter.getName()), String.valueOf(facetParameter.getLimit()));
    }
  }

  private static void appendANDOperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" AND ");
    }
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values) {
    if (!values.isEmpty()) {
      appendANDOperator(ret, true);

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

  private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

    ret.append("(").append(key).append(": ").append(appendDoubleQuotes ? "\"" : "").append(value)
      .append(appendDoubleQuotes ? "\"" : "").append(")");
  }

  private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (StringUtils.isBlank(value)) {
      appendExactMatch(ret, key, "*", false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (value.matches("^\".+\"$")) {
      appendExactMatch(ret, key, value, false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      appendWhiteSpaceTokenizedString(ret, key, value, operator);
    }
  }

  // FIXME escape values for Solr special chars
  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator) {
    appendANDOperator(ret, true);

    String[] split = value.trim().split("\\s+");
    ret.append("(");
    for (int i = 0; i < split.length; i++) {
      if (i != 0 && operator != null) {
        ret.append(" " + operator + " ");
      }
      ret.append(key).append(": ").append(escapeSolrSpecialChars(split[i]));
    }
    ret.append(")");
  }

  /**
   * Method that knows how to escape characters for Solr
   * <p>
   * <code>+ - && || ! ( ) { } [ ] ^ " ~ * ? : \</code>
   * </p>
   * <p>
   * Note: chars <code>'-', '"' and '*'</code> are not being escaped on purpose
   * </p>
   * 
   * @return a string with special characters escaped
   */
  // FIXME perhaps && and || are not being properly escaped: see how to do it
  public static String escapeSolrSpecialChars(String string) {
    return string.replaceAll("([+&|!(){}\\[\\]\\^\\\\~?:])", "\\\\$1");
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
      LOGGER.error("Could not convert Solr object to float" + object.getClass().getName());
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
        LOGGER.debug("Parsing date (" + object + ") from string");
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
    if (object == null) {
      ret = null;
    } else if (object instanceof Boolean) {
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

  // TODO: Handle SimpleRepresentationPreservationMetadata
  private static <T> String getIndexName(Class<T> resultClass) throws IndexServiceException {
    String indexName;
    if (resultClass.equals(AIP.class)) {
      indexName = RodaConstants.INDEX_AIP;
    } else if (resultClass.equals(SimpleDescriptionObject.class)) {
      indexName = RodaConstants.INDEX_SDO;
    } else if (resultClass.equals(Representation.class)) {
      indexName = RodaConstants.INDEX_REPRESENTATIONS;
    } else if (resultClass.equals(SimpleRepresentationFilePreservationMetadata.class)) {
      indexName = RodaConstants.INDEX_PRESERVATION_OBJECTS;
    } else if (resultClass.equals(SimpleEventPreservationMetadata.class)) {
      indexName = RodaConstants.INDEX_PRESERVATION_EVENTS;
    } else if (resultClass.equals(LogEntry.class)) {
      indexName = RodaConstants.INDEX_ACTION_LOG;
    } else if (resultClass.equals(SIPReport.class)) {
      indexName = RodaConstants.INDEX_SIP_REPORT;
    } else if (resultClass.equals(User.class)) {
      LOGGER.warn("Use " + RODAMember.class.getCanonicalName() + " instead of " + User.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(Group.class)) {
      LOGGER.warn("Use " + RODAMember.class.getCanonicalName() + " instead of " + Group.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(RODAMember.class)) {
      indexName = RodaConstants.INDEX_MEMBERS;
    } else {
      throw new IndexServiceException("Cannot find class index name: " + resultClass.getName(),
        IndexServiceException.INTERNAL_SERVER_ERROR);
    }
    return indexName;
  }

  private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc) throws IndexServiceException {
    T ret;
    if (resultClass.equals(AIP.class)) {
      ret = resultClass.cast(solrDocumentToAIP(doc));
    } else if (resultClass.equals(SimpleDescriptionObject.class)) {
      ret = resultClass.cast(solrDocumentToSDO(doc));
    } else if (resultClass.equals(Representation.class)) {
      ret = resultClass.cast(solrDocumentToRepresentation(doc));
    } else if (resultClass.equals(SimpleRepresentationFilePreservationMetadata.class)) {
      ret = resultClass.cast(solrDocumentToSimpleRepresentationFileMetadata(doc));
    } else if (resultClass.equals(SimpleEventPreservationMetadata.class)) {
      ret = resultClass.cast(solrDocumentToSimpleEventPreservationMetadata(doc));
    } else if (resultClass.equals(LogEntry.class)) {
      ret = resultClass.cast(solrDocumentToLogEntry(doc));
    } else if (resultClass.equals(SIPReport.class)) {
      ret = resultClass.cast(solrDocumentToSipState(doc));
    } else
      if (resultClass.equals(RODAMember.class) || resultClass.equals(User.class) || resultClass.equals(Group.class)) {
      ret = resultClass.cast(solrDocumentToRodaMember(doc));
    } else {
      throw new IndexServiceException("Cannot find class index name: " + resultClass.getName(),
        IndexServiceException.INTERNAL_SERVER_ERROR);
    }
    return ret;

  }

  public static <T> T retrieve(SolrClient index, Class<T> classToRetrieve, String... ids) throws IndexServiceException {
    T ret;
    try {
      SolrDocument doc = index.getById(getIndexName(classToRetrieve), SolrUtils.getId(ids));
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc);
      } else {
        throw new IndexServiceException("Document not found", IndexServiceException.NOT_FOUND);
      }
    } catch (SolrServerException | IOException e) {
      throw new IndexServiceException("Could not retrieve AIP from index", IndexServiceException.INTERNAL_SERVER_ERROR,
        e);
    }
    return ret;
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist) throws IndexServiceException {
    return find(index, classToRetrieve, filter, sorter, sublist, null);
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws IndexServiceException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    String queryString = parseFilter(filter);
    query.setQuery(queryString);
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);
    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | IOException e) {
      throw new IndexServiceException("Could not query index", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return ret;
  }

  public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
    throws IndexServiceException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  // FIXME see how to handle active
  public static AIP solrDocumentToAIP(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.AIP_ID));
    final Boolean active = objectToBoolean(doc.get(RodaConstants.AIP_ACTIVE));
    final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID));
    final Date dateCreated = objectToDate(doc.get(RodaConstants.AIP_DATE_CREATED));
    final Date dateModified = objectToDate(doc.get(RodaConstants.AIP_DATE_MODIFIED));
    final List<String> descriptiveMetadataFileIds = objectToListString(
      doc.get(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID));
    final List<String> representationIds = objectToListString(doc.get(RodaConstants.AIP_REPRESENTATION_ID));

    RODAObjectPermissions permissions = getPermissions(doc);
    /*
     * final List<String> preservationObjectsIds =
     * objectToListString(doc.get(RodaConstants.AIP_PRESERVATION_OBJECTS_ID) );
     * final List<String> preservationEventsIds =
     * objectToListString(doc.get(RodaConstants.AIP_PRESERVATION_EVENTS_ID)) ;
     */
    return new AIP(id, parentId, active, dateCreated, dateModified, permissions, descriptiveMetadataFileIds,
      representationIds, null, null, null);
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

    setPermissions(aip, ret);

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

    RODAObjectPermissions permissions = getPermissions(doc);

    return new SimpleDescriptionObject(id, label, dateModified, dateCreated, state, level, title, dateInitial,
      dateFinal, description, parentId, childrenCount, permissions);

  }

  public static SolrInputDocument aipToSolrInputDocumentAsSDO(AIP aip, ModelService model)
    throws ModelServiceException, StorageServiceException, IndexServiceException {
    final SolrInputDocument ret = new SolrInputDocument();
    final String aipId = aip.getId();

    ret.addField(RodaConstants.AIP_ID, aipId);
    ret.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
    ret.addField(RodaConstants.AIP_ACTIVE, aip.isActive());
    ret.addField(RodaConstants.AIP_DATE_CREATED, aip.getDateCreated());
    ret.addField(RodaConstants.AIP_DATE_MODIFIED, aip.getDateModified());

    // TODO see if this really should be indexed into SDO
    ret.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, aip.getDescriptiveMetadataIds());
    ret.addField(RodaConstants.AIP_REPRESENTATION_ID, aip.getRepresentationIds());
    ret.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, !aip.getRepresentationIds().isEmpty());

    for (String descId : aip.getDescriptiveMetadataIds()) {
      DescriptiveMetadata metadata = model.retrieveDescriptiveMetadata(aipId, descId);
      StoragePath storagePath = metadata.getStoragePath();
      Binary binary = model.getStorage().getBinary(storagePath);
      SolrInputDocument fields = getDescriptiveMetataFields(binary);

      for (SolrInputField field : fields) {
        ret.addField(field.getName(), field.getValue(), field.getBoost());
      }
    }

    // add permissions
    setPermissions(aip, ret);

    // TODO add information for SDO
    // TODO add sub-documents with full descriptive metadata info

    return ret;
  }

  private static RODAObjectPermissions getPermissions(SolrDocument doc) {
    RODAObjectPermissions permissions = new RODAObjectPermissions();

    List<String> list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_GRANT_USERS));
    permissions.setGrantUsers(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_GRANT_GROUPS));
    permissions.setGrantGroups(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_READ_USERS));
    permissions.setReadUsers(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_READ_GROUPS));
    permissions.setReadGroups(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_INSERT_USERS));
    permissions.setInsertUsers(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_INSERT_GROUPS));
    permissions.setInsertGroups(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_MODIFY_USERS));
    permissions.setModifyUsers(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_MODIFY_GROUPS));
    permissions.setModifyGroups(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_REMOVE_USERS));
    permissions.setRemoveUsers(list);
    list = objectToListString(doc.get(RodaConstants.AIP_PERMISSION_REMOVE_GROUPS));
    permissions.setRemoveGroups(list);

    return permissions;
  }

  private static void setPermissions(AIP aip, final SolrInputDocument ret) {
    RODAObjectPermissions permissions = aip.getPermissions();
    ret.addField(RodaConstants.AIP_PERMISSION_GRANT_USERS, permissions.getGrantUsers());
    ret.addField(RodaConstants.AIP_PERMISSION_GRANT_GROUPS, permissions.getGrantGroups());
    ret.addField(RodaConstants.AIP_PERMISSION_READ_USERS, permissions.getReadUsers());
    ret.addField(RodaConstants.AIP_PERMISSION_READ_GROUPS, permissions.getReadGroups());
    ret.addField(RodaConstants.AIP_PERMISSION_INSERT_USERS, permissions.getInsertUsers());
    ret.addField(RodaConstants.AIP_PERMISSION_INSERT_GROUPS, permissions.getInsertGroups());
    ret.addField(RodaConstants.AIP_PERMISSION_MODIFY_USERS, permissions.getModifyUsers());
    ret.addField(RodaConstants.AIP_PERMISSION_MODIFY_GROUPS, permissions.getModifyGroups());
    ret.addField(RodaConstants.AIP_PERMISSION_REMOVE_USERS, permissions.getRemoveUsers());
    ret.addField(RodaConstants.AIP_PERMISSION_REMOVE_GROUPS, permissions.getRemoveGroups());
  }

  public static Representation solrDocumentToRepresentation(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.SRO_ID));
    final String aipId = objectToString(doc.get(RodaConstants.SRO_AIP_ID));
    final Boolean active = objectToBoolean(doc.get(RodaConstants.SRO_ACTIVE));
    final Long sizeInBytes = objectToLong(doc.get(RodaConstants.SRO_SIZE_IN_BYTES));
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
    return new Representation(id, aipId, active, dateCreated, dateModified, statuses, type, sizeInBytes, fileIds);
  }

  public static SolrInputDocument representationToSolrDocument(Representation rep) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.SRO_UUID, getId(rep.getAipId(), rep.getId()));
    doc.addField(RodaConstants.SRO_ID, rep.getId());
    doc.addField(RodaConstants.SRO_AIP_ID, rep.getAipId());
    doc.addField(RodaConstants.SRO_SIZE_IN_BYTES, rep.getSizeInBytes());
    doc.addField(RodaConstants.SRO_DATE_CREATION, rep.getDateCreated());
    doc.addField(RodaConstants.SRO_DATE_MODIFICATION, rep.getDateModified());
    if (rep.getStatuses() != null && !rep.getStatuses().isEmpty()) {
      for (RepresentationState rs : rep.getStatuses()) {
        doc.addField(RodaConstants.SRO_STATUS, rs.toString());
      }
    }
    doc.addField(RodaConstants.SRO_TYPE, rep.getType());

    doc.addField(RodaConstants.SRO_FILE_IDS, rep.getFileIds());
    return doc;
  }

  public static SimpleEventPreservationMetadata solrDocumentToSimpleEventPreservationMetadata(SolrDocument doc) {
    final String agentID = objectToString(doc.get(RodaConstants.SEPM_AGENT_ID));
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
    sepm.setId(id);
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

  public static SimpleRepresentationPreservationMetadata solrDocumentToSimpleRepresentationPreservationMetadata(
    SolrDocument doc) {
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

  public static SimpleRepresentationFilePreservationMetadata solrDocumentToSimpleRepresentationFileMetadata(
    SolrDocument doc) {
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
    SimpleRepresentationFilePreservationMetadata srpm = new SimpleRepresentationFilePreservationMetadata();
    srpm.setCreatedDate(dateCreated);
    srpm.setFileId(id);
    srpm.setHash(hash);
    srpm.setId(id);
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

  public static SolrInputDocument representationPreservationObjectToSolrDocument(String id,
    RepresentationPreservationObject premisObject) {
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

  public static SolrInputDocument eventPreservationObjectToSolrDocument(String id,
    EventPreservationObject premisEvent) {
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

  public static SolrInputDocument representationFilePreservationObjectToSolrDocument(String id,
    RepresentationFilePreservationObject representationFile) {
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

  private static LogEntry solrDocumentToLogEntry(SolrDocument doc) {
    final String actionComponent = objectToString(doc.get(RodaConstants.LOG_ACTION_COMPONENT));
    final String actionMethod = objectToString(doc.get(RodaConstants.LOG_ACTION_METHOD));
    final String address = objectToString(doc.get(RodaConstants.LOG_ADDRESS));
    final Date datetime = objectToDate(doc.get(RodaConstants.LOG_DATETIME));
    final long duration = objectToLong(doc.get(RodaConstants.LOG_DURATION));
    final String id = objectToString(doc.get(RodaConstants.LOG_ID));
    // final String parameters =
    // objectToString(docisUser.get(RodaConstants.LOG_PARAMETERS));
    final String relatedObjectId = objectToString(doc.get(RodaConstants.LOG_RELATED_OBJECT_ID));
    final String username = objectToString(doc.get(RodaConstants.LOG_USERNAME));
    LogEntry entry = new LogEntry();
    entry.setActionComponent(actionComponent);
    entry.setActionMethod(actionMethod);
    entry.setAddress(address);
    entry.setDatetime(datetime);
    entry.setDuration(duration);
    entry.setId(id);
    // entry.setParameters(fromJson(parameters));
    entry.setRelatedObjectID(relatedObjectId);
    entry.setUsername(username);

    return entry;
  }

  public static SolrInputDocument logEntryToSolrDocument(LogEntry logEntry) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.LOG_ACTION_COMPONENT, logEntry.getActionComponent());
    doc.addField(RodaConstants.LOG_ACTION_METHOD, logEntry.getActionMethod());
    doc.addField(RodaConstants.LOG_ADDRESS, logEntry.getAddress());
    doc.addField(RodaConstants.LOG_DATETIME, logEntry.getDatetime());
    doc.addField(RodaConstants.LOG_DURATION, logEntry.getDuration());
    doc.addField(RodaConstants.LOG_ID, logEntry.getId());
    // doc.addField(RodaConstants.LOG_PARAMETERS,
    // toJSON(logEntry.getParameters()));
    doc.addField(RodaConstants.LOG_RELATED_OBJECT_ID, logEntry.getRelatedObjectID());
    doc.addField(RodaConstants.LOG_USERNAME, logEntry.getUsername());
    return doc;
  }

  private static SIPReport solrDocumentToSipState(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.SIP_REPORT_ID));
    final String username = objectToString(doc.get(RodaConstants.SIP_REPORT_USERNAME));
    final String originalFilename = objectToString(doc.get(RodaConstants.SIP_REPORT_ORIGINAL_FILENAME));
    final String state = objectToString(doc.get(RodaConstants.SIP_REPORT_STATE));
    final Date dateTime = objectToDate(doc.get(RodaConstants.SIP_REPORT_DATETIME));
    final boolean processing = objectToBoolean(doc.get(RodaConstants.SIP_REPORT_PROCESSING));
    final boolean complete = objectToBoolean(doc.get(RodaConstants.SIP_REPORT_COMPLETE));
    final float completePercentage = objectToFloat(doc.get(RodaConstants.SIP_REPORT_COMPLETE_PERCENTAGE));
    final String parentPID = objectToString(doc.get(RodaConstants.SIP_REPORT_PARENT_PID));
    final String ingestedPID = objectToString(doc.get(RodaConstants.SIP_REPORT_INGESTED_PID));
    final String fileID = objectToString(doc.get(RodaConstants.SIP_REPORT_FILE_ID));
    List<SIPStateTransition> ssts = new ArrayList<SIPStateTransition>();
    /*
     * if(doc.getChildDocumentCount()>0){ for(SolrDocument child :
     * doc.getChildDocuments()){ SIPStateTransition sst = new
     * SIPStateTransition(); sst.setId(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_ID)));
     * sst.setDatetime(objectToDate(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_DATETIME)));
     * sst.setDescription(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_DESCRIPTION)));
     * sst.setFromState(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_FROM)));
     * sst.setSipID(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_SIPID)));
     * sst.setSuccess(objectToBoolean(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_SUCCESS)));
     * sst.setTaskID(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_TASKID)));
     * sst.setToState(objectToString(child.get(RodaConstants.
     * SIPSTATE_TRANSITION_TO))); ssts.add(sst); } }
     */

    SIPReport sipState = new SIPReport();
    sipState.setId(id);
    sipState.setUsername(username);
    sipState.setOriginalFilename(originalFilename);
    sipState.setState(state);
    sipState.setDatetime(dateTime);
    sipState.setProcessing(processing);
    sipState.setComplete(complete);
    sipState.setCompletePercentage(completePercentage);
    sipState.setParentID(parentPID);
    sipState.setIngestedID(ingestedPID);
    sipState.setFileID(fileID);
    sipState.setStateTransitions(ssts.toArray(new SIPStateTransition[ssts.size()]));
    return sipState;
  }

  public static SolrInputDocument sipReportToSolrDocument(SIPReport sipState) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.SIP_REPORT_COMPLETE, sipState.isComplete());
    doc.addField(RodaConstants.SIP_REPORT_COMPLETE_PERCENTAGE, sipState.getCompletePercentage());
    doc.addField(RodaConstants.SIP_REPORT_DATETIME, sipState.getDatetime());
    doc.addField(RodaConstants.SIP_REPORT_FILE_ID, sipState.getFileID());
    doc.addField(RodaConstants.SIP_REPORT_ID, sipState.getId());
    doc.addField(RodaConstants.SIP_REPORT_INGESTED_PID, sipState.getIngestedID());
    doc.addField(RodaConstants.SIP_REPORT_ORIGINAL_FILENAME, sipState.getOriginalFilename());
    doc.addField(RodaConstants.SIP_REPORT_PARENT_PID, sipState.getParentID());
    doc.addField(RodaConstants.SIP_REPORT_PROCESSING, sipState.isProcessing());
    doc.addField(RodaConstants.SIP_REPORT_STATE, sipState.getState());
    doc.addField(RodaConstants.SIP_REPORT_USERNAME, sipState.getUsername());
    /*
     * if(sipState.getStateTransitions()!=null &&
     * sipState.getStateTransitions().length>0){ for(SIPStateTransition sst :
     * sipState.getStateTransitions()){ SolrInputDocument sstSolrDoc = new
     * SolrInputDocument();
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_DATETIME,
     * sst.getDatetime());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_DESCRIPTION,
     * sst.getDescription());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_FROM,
     * sst.getFromState());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_SIPID,
     * sst.getSipID());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_SUCCESS,
     * sst.isSuccess());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_TASKID,
     * sst.getTaskID());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_TO,
     * sst.getToState());
     * sstSolrDoc.addField(RodaConstants.SIPSTATE_TRANSITION_ID, sst.getId());
     * doc.addChildDocument(sstSolrDoc); } }
     */
    return doc;
  }

  public static SolrInputDocument rodaMemberToSolrDocument(RODAMember member) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, member.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, member.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, member.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, member.getName());
    if (member.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(member.getAllGroups()));
    }
    if (member.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(member.getAllRoles()));
    }

    return doc;
  }

  private static RODAMember solrDocumentToRodaMember(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.MEMBERS_ID));
    final boolean isActive = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE));
    final boolean isUser = objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER));
    final String name = objectToString(doc.get(RodaConstants.MEMBERS_NAME));
    final Set<String> groups = new HashSet<String>();
    List<String> possibleGroups = objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS_ALL));
    if (possibleGroups != null) {
      groups.addAll(possibleGroups);
    }
    final Set<String> roles = new HashSet<String>();
    List<String> possibleRoles = objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL));
    if (possibleRoles != null) {
      roles.addAll(possibleRoles);
    }
    if (isUser) {
      User user = new User();
      user.setId(id);
      user.setActive(isActive);
      user.setAllGroups(groups);
      user.setAllRoles(roles);
      user.setActive(isActive);
      user.setName(name);
      // TODO get all user other info
      return user;
    } else {
      Group group = new Group();
      group.setId(id);
      group.setActive(isActive);
      group.setAllGroups(groups);
      group.setAllRoles(roles);
      group.setActive(isActive);
      group.setName(name);
      return group;
    }
  }

  public static SolrInputDocument userToSolrDocument(User user) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, user.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, user.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, user.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, user.getName());
    if (user.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(user.getAllGroups()));
    }
    if (user.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(user.getAllRoles()));
    }

    return doc;
  }

  // private static User solrDocumentToUser(SolrDocument doc) {
  // final String id = objectToString(doc.get(RodaConstants.MEMBERS_ID));
  // final boolean isActive =
  // objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE));
  // final boolean isUser =
  // objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER));
  // final String name = objectToString(doc.get(RodaConstants.MEMBERS_NAME));
  // final Set<String> groups = new
  // HashSet<String>(objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS_ALL)));
  // final Set<String> roles = new
  // HashSet<String>(objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL)));
  // User user = new User();
  // user.setId(id);
  // user.setActive(isActive);
  // user.setAllGroups(groups);
  // user.setAllRoles(roles);
  // user.setActive(isActive);
  // user.setName(name);
  // return user;
  // }

  public static SolrInputDocument groupToSolrDocument(Group group) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.MEMBERS_ID, group.getId());
    doc.addField(RodaConstants.MEMBERS_IS_ACTIVE, group.isActive());
    doc.addField(RodaConstants.MEMBERS_IS_USER, group.isUser());
    doc.addField(RodaConstants.MEMBERS_NAME, group.getName());
    if (group.getAllGroups() != null) {
      doc.addField(RodaConstants.MEMBERS_GROUPS_ALL, new ArrayList<String>(group.getAllGroups()));
    }
    if (group.getAllRoles() != null) {
      doc.addField(RodaConstants.MEMBERS_ROLES_ALL, new ArrayList<String>(group.getAllRoles()));
    }

    return doc;
  }

  // private static Group solrDocumentToGroup(SolrDocument doc) {
  // final String id = objectToString(doc.get(RodaConstants.MEMBERS_ID));
  // final boolean isActive =
  // objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_ACTIVE));
  // final boolean isUser =
  // objectToBoolean(doc.get(RodaConstants.MEMBERS_IS_USER));
  // final String name = objectToString(doc.get(RodaConstants.MEMBERS_NAME));
  // final Set<String> groups = new
  // HashSet<String>(objectToListString(doc.get(RodaConstants.MEMBERS_GROUPS_ALL)));
  // final Set<String> roles = new
  // HashSet<String>(objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL)));
  // Group group = new Group();
  // group.setActive(isActive);
  // group.setAllGroups(groups);
  // group.setAllRoles(roles);
  // group.setActive(isActive);
  // group.setName(name);
  // return group;
  // }
}
