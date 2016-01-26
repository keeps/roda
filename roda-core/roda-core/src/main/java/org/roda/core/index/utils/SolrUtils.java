/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
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
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.adapter.facet.FacetParameter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.RangeFacetParameter;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.LongRangeFilterParameter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DateGranularity;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.FacetFieldResult;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.RODAObjectPermissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.RepresentationFilePreservationObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaGroup;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    Class<T> responseClass, Facets facets) throws GenericException {
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

  public static SolrInputDocument getDescriptiveMetataFields(Binary binary) throws GenericException {
    SolrInputDocument doc;
    InputStream inputStream;
    String xsltFilename = null;
    InputStream transformerStream = null;

    try {

      // get xslt from metadata type if defined
      if (binary.getMetadata() != null) {
        if (binary.getMetadata().get(RodaConstants.STORAGE_META_TYPE) != null
          && binary.getMetadata().get(RodaConstants.STORAGE_META_TYPE).size() > 0) {
          String metadataType = binary.getMetadata().get(RodaConstants.STORAGE_META_TYPE).iterator().next();
          if (metadataType != null) {
            String lowerCaseMetadataType = metadataType.toLowerCase();
            transformerStream = RodaCoreFactory
              .getConfigurationFileAsStream("crosswalks/ingest/" + lowerCaseMetadataType + ".xslt");
          }
        }
      }
      // get xslt from filename
      if (transformerStream == null) {
        String filename = FilenameUtils.removeExtension(binary.getStoragePath().getName());
        if (filename != null) {
          filename = filename.toLowerCase();
          transformerStream = RodaCoreFactory.getConfigurationFileAsStream("crosswalks/ingest/" + filename + ".xslt");
        }
      }
      // fallback
      if (transformerStream == null) {
        transformerStream = RodaCoreFactory.getConfigurationFileAsStream("crosswalks/ingest/" + "plain.xslt");
      }

      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);

      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
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
      throw new GenericException(
        "Could not process descriptive metadata binary " + binary.getStoragePath() + " using xslt " + xsltFilename, e);
    }
    return validateDescriptiveMetadataFields(doc);
  }

  private static SolrInputDocument validateDescriptiveMetadataFields(SolrInputDocument doc) {
    SimpleDateFormat df = new SimpleDateFormat(RodaConstants.SOLRDATEFORMAT);
    if (doc.get(RodaConstants.AIP_DATE_INITIAL) != null) {
      Object value = doc.get(RodaConstants.AIP_DATE_INITIAL).getValue();
      if (value instanceof String) {
        try {
          Date d = df.parse((String) value);
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
          Date d = df.parse((String) value);
          doc.setField(RodaConstants.AIP_DATE_FINAL, d);
        } catch (ParseException pe) {
          doc.remove(RodaConstants.AIP_DATE_FINAL);
          doc.setField(RodaConstants.AIP_DATE_FINAL + "_txt", value);
        }
      }
    }
    return doc;
  }

  public static String parseFilter(Filter filter) throws RequestNotValidException {
    StringBuilder ret = new StringBuilder();

    if (filter == null || filter.getParameters().isEmpty()) {
      ret.append("*:*");
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        parseFilterParameter(ret, parameter);
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.debug("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter)
    throws RequestNotValidException {
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
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
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

  public static List<SortClause> parseSorter(Sorter sorter) {
    List<SortClause> ret = new ArrayList<SortClause>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SortClause(sortParameter.getName(), sortParameter.isDescending() ? ORDER.desc : ORDER.asc));
      }
    }
    return ret;
  }

  private static void parseAndConfigureFacets(Facets facets, SolrQuery query) {
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

    ret.append("(").append(key).append(": ").append(appendDoubleQuotes ? "\"" : "")
      .append(value.replaceAll("(\")", "\\\\$1")).append(appendDoubleQuotes ? "\"" : "").append(")");
  }

  private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (StringUtils.isBlank(value)) {
      appendExactMatch(ret, key, "*", false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (value.matches("^\".+\"$")) {
      appendExactMatch(ret, key, value.substring(1, value.length() - 1), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      appendWhiteSpaceTokenizedString(ret, key, value, operator);
    }
  }

  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator) {
    appendANDOperator(ret, true);

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
    return string.replaceAll("([+&|!(){}\\[\\]\\^\\\\~?:\"])", "\\\\$1");
  }

  private static List<String> objectToListString(Object object) {
    List<String> ret;
    if (object == null) {
      ret = new ArrayList<String>();
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
      ret = new ArrayList<String>();
    }
    return ret;
  }

  public static Integer objectToInteger(Object object) {
    Integer ret;
    if (object instanceof Integer) {
      ret = (Integer) object;
    } else if (object instanceof String) {
      try {
        ret = Integer.parseInt((String) object);
      } catch (NumberFormatException e) {
        LOGGER.error("Could not convert Solr object to integer", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to integer" + object.getClass().getName());
      ret = null;
    }
    return ret;
  }

  public static Long objectToLong(Object object) {
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
    return objectToBoolean(object, null);
  }

  private static Boolean objectToBoolean(Object object, Boolean defaultValue) {
    Boolean ret;
    if (object == null) {
      ret = defaultValue;
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
  private static <T> String getIndexName(Class<T> resultClass) throws GenericException {
    String indexName;
    if (resultClass.equals(AIP.class)) {
      indexName = RodaConstants.INDEX_AIP;
    } else if (resultClass.equals(IndexedAIP.class)) {
      indexName = RodaConstants.INDEX_AIP;
    } else if (resultClass.equals(Representation.class)) {
      indexName = RodaConstants.INDEX_REPRESENTATION;
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      indexName = RodaConstants.INDEX_PRESERVATION_EVENTS;
    } else if (resultClass.equals(LogEntry.class)) {
      indexName = RodaConstants.INDEX_ACTION_LOG;
    } else if (resultClass.equals(JobReport.class)) {
      indexName = RodaConstants.INDEX_JOB_REPORT;
    } else if (resultClass.equals(User.class)) {
      LOGGER.warn("Use " + RODAMember.class.getCanonicalName() + " instead of " + User.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(Group.class)) {
      LOGGER.warn("Use " + RODAMember.class.getCanonicalName() + " instead of " + Group.class.getCanonicalName());
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(RODAMember.class)) {
      indexName = RodaConstants.INDEX_MEMBERS;
    } else if (resultClass.equals(TransferredResource.class)) {
      indexName = RodaConstants.INDEX_TRANSFERRED_RESOURCE;
    } else if (resultClass.equals(Job.class)) {
      indexName = RodaConstants.INDEX_JOB;
    } else if (resultClass.equals(IndexedFile.class)) {
      indexName = RodaConstants.INDEX_FILE;
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return indexName;
  }

  private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc) throws GenericException {
    T ret;
    if (resultClass.equals(IndexedAIP.class)) {
      ret = resultClass.cast(solrDocumentToIndexAIP(doc));
    } else if (resultClass.equals(Representation.class)) {
      ret = resultClass.cast(solrDocumentToRepresentation(doc));
    } else if (resultClass.equals(LogEntry.class)) {
      ret = resultClass.cast(solrDocumentToLogEntry(doc));
    } else if (resultClass.equals(JobReport.class)) {
      ret = resultClass.cast(solrDocumentToJobReport(doc));
    } else if (resultClass.equals(RODAMember.class) || resultClass.equals(User.class)
      || resultClass.equals(Group.class)) {
      ret = resultClass.cast(solrDocumentToRodaMember(doc));
    } else if (resultClass.equals(TransferredResource.class)) {
      ret = resultClass.cast(solrDocumentToTransferredResource(doc));
    } else if (resultClass.equals(Job.class)) {
      ret = resultClass.cast(solrDocumentToJob(doc));
    } else if (resultClass.equals(IndexedFile.class)) {
      ret = resultClass.cast(solrDocumentToSimpleFile(doc));
    } else if (resultClass.equals(IndexedPreservationEvent.class)) {
      ret = resultClass.cast(solrDocumentToIndexedPreservationEvent(doc));
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return ret;
  }

  public static <T> T retrieve(SolrClient index, Class<T> classToRetrieve, String... ids)
    throws NotFoundException, GenericException {
    T ret;
    String id = SolrUtils.getId(ids);
    try {
      SolrDocument doc = index.getById(getIndexName(classToRetrieve), SolrUtils.getId(ids));
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc);
      } else {
        throw new NotFoundException("Could not find document " + id);
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve AIP from index", e);
    }
    return ret;
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, sorter, sublist, null);
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    String queryString = parseFilter(filter);
    query.setQuery(queryString);
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);

    LOGGER.debug("Solr Query: " + query);

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
    throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  private static final Set<String> NON_REPEATABLE_FIELDS = new HashSet<>(
    Arrays.asList("title", "level", "dateInitial", "dateFinal"));

  // FIXME see how to handle active and all the other that are not being put in
  // the indexedaip
  public static IndexedAIP solrDocumentToIndexAIP(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.AIP_ID));
    final Boolean active = objectToBoolean(doc.get(RodaConstants.AIP_ACTIVE));
    final AIPState state = active ? AIPState.ACTIVE : AIPState.INACTIVE;
    final String parentId = objectToString(doc.get(RodaConstants.AIP_PARENT_ID));
    final List<String> descriptiveMetadataFileIds = objectToListString(
      doc.get(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID));
    final List<String> representationIds = objectToListString(doc.get(RodaConstants.AIP_REPRESENTATION_ID));

    RODAObjectPermissions permissions = getPermissions(doc);

    // FIXME this information is not being recorded. passing by empty
    // collections for easier processing
    final Map<String, List<String>> preservationRepresentationObjectsIds = new HashMap<String, List<String>>();
    final Map<String, List<String>> preservationEventsIds = new HashMap<String, List<String>>();
    final Map<String, List<String>> preservationFileObjectsIds = new HashMap<String, List<String>>();

    final List<String> levels = objectToListString(doc.get(RodaConstants.AIP_LEVEL));
    final List<String> titles = objectToListString(doc.get(RodaConstants.AIP_TITLE));
    final List<String> descriptions = objectToListString(doc.get(RodaConstants.AIP_DESCRIPTION));
    final Date dateInitial = objectToDate(doc.get(RodaConstants.AIP_DATE_INITIAL));
    final Date dateFinal = objectToDate(doc.get(RodaConstants.AIP_DATE_FINAL));

    final String level = levels.isEmpty() ? null : levels.get(0);
    final String title = titles.isEmpty() ? null : titles.get(0);
    final String description = descriptions.isEmpty() ? null : descriptions.get(0);
    final int childrenCount = 0;

    return new IndexedAIP(id, state, level, title, dateInitial, dateFinal, description, parentId, childrenCount,
      permissions);
  }

  public static SolrInputDocument aipToSolrInputDocument(AIP aip, ModelService model, boolean safemode)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument ret = new SolrInputDocument();

    ret.addField(RodaConstants.AIP_ID, aip.getId());
    ret.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
    ret.addField(RodaConstants.AIP_ACTIVE, aip.isActive());

    List<String> descriptiveMetadataIds = aip.getMetadata().getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());

    ret.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, descriptiveMetadataIds);
    ret.addField(RodaConstants.AIP_REPRESENTATION_ID, aip.getRepresentationIds());
    ret.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, !aip.getRepresentationIds().isEmpty());

    setPermissions(aip, ret);

    if (!safemode) {
      // guarding against repeated fields
      Set<String> usedNonRepeatableFields = new HashSet<>();

      for (DescriptiveMetadata metadata : aip.getMetadata().getDescriptiveMetadata()) {
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aip.getId(), metadata.getId());
        Binary binary = model.getStorage().getBinary(storagePath);
        try {
          SolrInputDocument fields = getDescriptiveMetataFields(binary);
          for (SolrInputField field : fields) {
            if (NON_REPEATABLE_FIELDS.contains(field.getName())) {
              boolean added = usedNonRepeatableFields.add(field.getName());
              if (added) {
                ret.addField(field.getName(), field.getValue(), field.getBoost());
              }
            } else {
              ret.addField(field.getName(), field.getValue(), field.getBoost());
            }
          }
        } catch (GenericException ise) {
          // TODO index the index errors for later processing
          LOGGER.warn("Error processing descriptive metadata: " + metadata);
        } catch (Throwable e) {
          LOGGER.error("Error processing descriptive metadata: " + metadata, e);
        }
      }
    }

    return ret;
  }

  private static RODAObjectPermissions getPermissions(SolrDocument doc) {
    RODAObjectPermissions permissions = new RODAObjectPermissions();
    // TODO get information from aip.json or METS.xml

    // List<String> list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_GRANT_USERS));
    // permissions.setGrantUsers(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_GRANT_GROUPS));
    // permissions.setGrantGroups(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_READ_USERS));
    // permissions.setReadUsers(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_READ_GROUPS));
    // permissions.setReadGroups(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_INSERT_USERS));
    // permissions.setInsertUsers(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_INSERT_GROUPS));
    // permissions.setInsertGroups(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_MODIFY_USERS));
    // permissions.setModifyUsers(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_MODIFY_GROUPS));
    // permissions.setModifyGroups(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_REMOVE_USERS));
    // permissions.setRemoveUsers(list);
    // list =
    // objectToListString(doc.get(RodaConstants.AIP_PERMISSION_REMOVE_GROUPS));
    // permissions.setRemoveGroups(list);

    return permissions;
  }

  private static void setPermissions(AIP aip, final SolrInputDocument ret) {
    RODAObjectPermissions permissions = aip.getPermissions();
    // TODO set this information into aip.json or METS.xml
    // ret.addField(RodaConstants.AIP_PERMISSION_GRANT_USERS,
    // permissions.getGrantUsers());
    // ret.addField(RodaConstants.AIP_PERMISSION_GRANT_GROUPS,
    // permissions.getGrantGroups());
    // ret.addField(RodaConstants.AIP_PERMISSION_READ_USERS,
    // permissions.getReadUsers());
    // ret.addField(RodaConstants.AIP_PERMISSION_READ_GROUPS,
    // permissions.getReadGroups());
    // ret.addField(RodaConstants.AIP_PERMISSION_INSERT_USERS,
    // permissions.getInsertUsers());
    // ret.addField(RodaConstants.AIP_PERMISSION_INSERT_GROUPS,
    // permissions.getInsertGroups());
    // ret.addField(RodaConstants.AIP_PERMISSION_MODIFY_USERS,
    // permissions.getModifyUsers());
    // ret.addField(RodaConstants.AIP_PERMISSION_MODIFY_GROUPS,
    // permissions.getModifyGroups());
    // ret.addField(RodaConstants.AIP_PERMISSION_REMOVE_USERS,
    // permissions.getRemoveUsers());
    // ret.addField(RodaConstants.AIP_PERMISSION_REMOVE_GROUPS,
    // permissions.getRemoveGroups());
  }

  public static IndexedRepresentation solrDocumentToRepresentation(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.SRO_ID));
    final String aipId = objectToString(doc.get(RodaConstants.SRO_AIP_ID));
    final boolean original = objectToBoolean(doc.get(RodaConstants.SRO_ORIGINAL), Boolean.FALSE);
    final List<String> fileIds = objectToListString(doc.get(RodaConstants.SRO_FILE_IDS));

    final Long sizeInBytes = objectToLong(doc.get(RodaConstants.SRO_SIZE_IN_BYTES));
    final Long totalNumberOfFiles = objectToLong(doc.get(RodaConstants.SRO_SIZE_IN_BYTES));

    return new IndexedRepresentation(id, aipId, original, fileIds, sizeInBytes, totalNumberOfFiles);
  }

  public static SolrInputDocument representationToSolrDocument(Representation rep) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.SRO_UUID, getId(rep.getAipId(), rep.getId()));
    doc.addField(RodaConstants.SRO_ID, rep.getId());
    doc.addField(RodaConstants.SRO_AIP_ID, rep.getAipId());

    // TODO calculate all files or make it obvious it is only direct files
    doc.addField(RodaConstants.SRO_FILE_IDS, rep.getFilesDirectlyUnder());

    // TODO calculate representation storage size or get this information from
    // somewhere
    doc.addField(RodaConstants.SRO_SIZE_IN_BYTES, 0L);

    return doc;
  }

  private static LogEntry solrDocumentToLogEntry(SolrDocument doc) {
    final String actionComponent = objectToString(doc.get(RodaConstants.LOG_ACTION_COMPONENT));
    final String actionMethod = objectToString(doc.get(RodaConstants.LOG_ACTION_METHOD));
    final String address = objectToString(doc.get(RodaConstants.LOG_ADDRESS));
    final Date datetime = objectToDate(doc.get(RodaConstants.LOG_DATETIME));
    final long duration = objectToLong(doc.get(RodaConstants.LOG_DURATION));
    final String id = objectToString(doc.get(RodaConstants.LOG_ID));
    final String parameters = objectToString(doc.get(RodaConstants.LOG_PARAMETERS));
    final String relatedObjectId = objectToString(doc.get(RodaConstants.LOG_RELATED_OBJECT_ID));
    final String username = objectToString(doc.get(RodaConstants.LOG_USERNAME));
    LogEntry entry = new LogEntry();
    entry.setActionComponent(actionComponent);
    entry.setActionMethod(actionMethod);
    entry.setAddress(address);
    entry.setDatetime(datetime);
    entry.setDuration(duration);
    entry.setId(id);
    entry.setParameters(ModelUtils.getLogEntryParameters(parameters == null ? "" : parameters));
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
    doc.addField(RodaConstants.LOG_PARAMETERS, ModelUtils.getJsonLogEntryParameters(logEntry.getParameters()));
    doc.addField(RodaConstants.LOG_RELATED_OBJECT_ID, logEntry.getRelatedObjectID());
    doc.addField(RodaConstants.LOG_USERNAME, logEntry.getUsername());
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
    groups.addAll(possibleGroups);
    final Set<String> roles = new HashSet<String>();
    List<String> possibleRoles = objectToListString(doc.get(RodaConstants.MEMBERS_ROLES_ALL));
    roles.addAll(possibleRoles);
    if (isUser) {
      RodaUser user = new RodaUser();
      user.setId(id);
      user.setActive(isActive);
      user.setAllGroups(groups);
      user.setAllRoles(roles);
      user.setActive(isActive);
      user.setName(name);
      return user;
    } else {
      RodaGroup group = new RodaGroup();
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

  /**
   * WARNING: this should only be used to debug/tests only
   * 
   * @return
   * @throws IOException
   * @throws SolrServerException
   */
  public static QueryResponse executeSolrQuery(SolrClient index, String collection, String solrQueryString)
    throws SolrServerException, IOException {
    LOGGER.debug("query string: " + solrQueryString);
    SolrQuery query = new SolrQuery();
    for (String string : solrQueryString.split("&")) {
      String[] split = string.split("=");
      LOGGER.debug(split[0] + " > " + split[1]);
      query.add(split[0], split[1]);
    }
    LOGGER.debug("executeSolrQuery: " + query);
    return index.query(collection, query);
  }

  private static IndexedPreservationEvent solrDocumentToIndexedPreservationEvent(SolrDocument doc) {
    final String id = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_ID));
    final String aipID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_AIP_ID));
    final String representationID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_ID));
    final String fileID = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_FILE_ID));
    final Date eventDateTime = objectToDate(doc.get(RodaConstants.PRESERVATION_EVENT_DATETIME));
    final String eventDetail = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_DETAIL));
    final String eventType = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_TYPE));
    final String eventOutcome = objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME));
    final String eventOutcomeDetailExtension = objectToString(
      doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME_DETAIL_EXTENSION));

    IndexedPreservationEvent ipe = new IndexedPreservationEvent();
    ipe.setId(id);
    ipe.setAipId(aipID);
    ipe.setRepresentationId(representationID);
    ipe.setFileId(fileID);
    ipe.setEventDateTime(eventDateTime);
    ipe.setEventDetail(eventDetail);
    ipe.setEventType(eventType);
    ipe.setEventOutcome(eventOutcome);
    ipe.setEventOutcomeDetailExtension(eventOutcomeDetailExtension);
    
    return ipe;
  }

  public static SolrInputDocument premisToSolr(String aipID, String representationID, String fileID, Binary binary)
    throws GenericException {
    SolrInputDocument doc;
    InputStream inputStream;
    try {
      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);

      InputStream transformerStream = RodaCoreFactory
        .getConfigurationFileAsStream("crosswalks/ingest/other/premis.xslt");
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      if(aipID!=null){
        stylesheetOpt.put("aipID", aipID);
      }
      if(representationID!=null){
        stylesheetOpt.put("representationID", representationID);
      }
      if(fileID!=null){
        stylesheetOpt.put("fileID", fileID);
      }
      RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
      descMetadataReader.close();

      XMLLoader loader = new XMLLoader();
      LOGGER.trace("Transformed premis metadata:\n" + transformerResult);
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
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath()
        + " using xslt " + "crosswalks/ingest/other/premis.xslt", e);
    }
    return doc;
  }

  private static TransferredResource solrDocumentToTransferredResource(SolrDocument doc) {
    TransferredResource tr = new TransferredResource();
    String id = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ID));
    String fullPath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH));
    String parentId = null;
    if (doc.containsKey(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)) {
      parentId = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));
    }
    String relativePath = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH));

    Date d = new Date();
    try {
      d = objectToDate(doc.get(RodaConstants.TRANSFERRED_RESOURCE_DATE));
    } catch (Exception e) {
      LOGGER.error("ERROR PARSING DATE: " + e.getMessage());
    }

    boolean isFile = objectToBoolean(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ISFILE));
    long size = objectToLong(doc.get(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
    String name = objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_NAME));

    List<String> ancestorsPath = objectToListString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS));

    tr.setCreationDate(d);
    tr.setFullPath(fullPath);
    tr.setId(id);
    tr.setName(name);
    tr.setRelativePath(relativePath);
    tr.setSize(size);
    tr.setParentId(parentId);
    tr.setFile(isFile);
    tr.setAncestorsPaths(ancestorsPath);
    return tr;
  }

  public static SolrInputDocument transferredResourceToSolrDocument(TransferredResource resource) throws IOException {
    SolrInputDocument transferredResource = new SolrInputDocument();

    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ID, resource.getId());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH, resource.getFullPath());
    if (resource.getParentId() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, resource.getParentId());
    }
    if (resource.getRelativePath() != null) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, resource.getRelativePath());
    }
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_DATE, resource.getCreationDate());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, resource.isFile());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_SIZE, resource.getSize());
    transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_NAME, resource.getName());
    if (resource.getAncestorsPaths() != null && resource.getAncestorsPaths().size() > 0) {
      transferredResource.addField(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, resource.getAncestorsPaths());
    }
    return transferredResource;
  }

  public static long getSizePath(Path startPath) throws IOException {
    final AtomicLong size = new AtomicLong(0);
    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        size.addAndGet(attrs.size());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }
    });
    return size.get();
  }

  public static SolrInputDocument jobToSolrDocument(Job job) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.JOB_ID, job.getId());
    doc.addField(RodaConstants.JOB_NAME, job.getName());
    doc.addField(RodaConstants.JOB_USERNAME, job.getUsername());
    doc.addField(RodaConstants.JOB_START_DATE, job.getStartDate());
    doc.addField(RodaConstants.JOB_END_DATE, job.getEndDate());
    doc.addField(RodaConstants.JOB_STATE, job.getState());
    doc.addField(RodaConstants.JOB_COMPLETION_PERCENTAGE, job.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_PLUGIN_TYPE, job.getPluginType());
    doc.addField(RodaConstants.JOB_PLUGIN, job.getPlugin());
    doc.addField(RodaConstants.JOB_PLUGIN_PARAMETERS, ModelUtils.getJsonFromObject(job.getPluginParameters()));
    doc.addField(RodaConstants.JOB_ORCHESTRATOR_METHOD, job.getOrchestratorMethod());
    doc.addField(RodaConstants.JOB_OBJECT_IDS, job.getObjectIds());

    return doc;
  }

  public static Job solrDocumentToJob(SolrDocument doc) {
    Job job = new Job();

    job.setId(objectToString(doc.get(RodaConstants.JOB_ID)));
    job.setName(objectToString(doc.get(RodaConstants.JOB_NAME)));
    job.setUsername(objectToString(doc.get(RodaConstants.JOB_USERNAME)));
    job.setStartDate(objectToDate(doc.get(RodaConstants.JOB_START_DATE)));
    job.setEndDate(objectToDate(doc.get(RodaConstants.JOB_END_DATE)));
    job.setState(JOB_STATE.valueOf(objectToString(doc.get(RodaConstants.JOB_STATE))));
    job.setCompletionPercentage(objectToInteger(doc.get(RodaConstants.JOB_COMPLETION_PERCENTAGE)));
    job.setPluginType(PluginType.valueOf(objectToString(doc.get(RodaConstants.JOB_PLUGIN_TYPE))));
    job.setPlugin(objectToString(doc.get(RodaConstants.JOB_PLUGIN)));
    job.setPluginParameters(ModelUtils.getMapFromJson(objectToString(doc.get(RodaConstants.JOB_PLUGIN_PARAMETERS))));
    job.setOrchestratorMethod(
      ORCHESTRATOR_METHOD.valueOf(objectToString(doc.get(RodaConstants.JOB_ORCHESTRATOR_METHOD))));
    job.setObjectIds(objectToListString(doc.get(RodaConstants.JOB_OBJECT_IDS)));

    return job;
  }

  public static SolrInputDocument fileToSolrDocument(File file, RepresentationFilePreservationObject premisFile,
    String fulltext) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(RodaConstants.FILE_UUID, getId(file.getAipId(), file.getRepresentationId(), file.getId()));
    doc.addField(RodaConstants.FILE_ID, getId(file.getAipId(), file.getRepresentationId(), file.getId()));
    doc.addField(RodaConstants.FILE_AIPID, file.getAipId());
    doc.addField(RodaConstants.FILE_FILEID, file.getId());
    doc.addField(RodaConstants.FILE_REPRESENTATIONID, file.getRepresentationId());
    doc.addField(RodaConstants.FILE_ISDIRECTORY, file.isDirectory());

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getRepresentationFilePath(file.getAipId(), file.getRepresentationId(),
        file.getId());
      doc.addField(RodaConstants.FILE_STORAGEPATH, filePath.asString());
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index file storage path", e);
    }

    // Add information from PREMIS
    if (premisFile != null) {
      // TODO get entry point from PREMIS or remove it
      // doc.addField(RodaConstants.FILE_ISENTRYPOINT, file.isEntryPoint());

      if (premisFile.getOriginalName() != null) {
        doc.addField(RodaConstants.FILE_ORIGINALNAME, premisFile.getOriginalName());
      }

      if (premisFile.getSize() != 0) {
        doc.addField(RodaConstants.FILE_SIZE, premisFile.getSize());
      }

      if (premisFile.getFixities() != null && premisFile.getFixities().length > 0) {
        List<String> hashes = new ArrayList<>();
        for (Fixity fixity : premisFile.getFixities()) {
          StringBuilder fixityPrint = new StringBuilder();
          fixityPrint.append(fixity.getMessageDigest());
          fixityPrint.append(" (");
          fixityPrint.append(fixity.getMessageDigestAlgorithm());
          // if (StringUtils.isNotBlank(fixity.getMessageDigestOriginator())) {
          // fixityPrint.append(", ");
          // fixityPrint.append(fixity.getMessageDigestOriginator());
          // }
          fixityPrint.append(")");

          hashes.add(fixityPrint.toString());
        }

        doc.addField(RodaConstants.FILE_HASH, hashes);
      }

      if (premisFile.getFormatDesignationName() != null) {
        doc.addField(RodaConstants.FILE_FILEFORMAT, premisFile.getFormatDesignationName());
      }
      if (premisFile.getFormatDesignationVersion() != null) {
        doc.addField(RodaConstants.FILE_FORMAT_VERSION, premisFile.getFormatDesignationVersion());
      }

      if (premisFile.getMimetype() != null) {
        doc.addField(RodaConstants.FILE_FORMAT_MIMETYPE, premisFile.getMimetype());
      }

      if (StringUtils.isNotBlank(premisFile.getPronomId())) {
        doc.addField(RodaConstants.FILE_PRONOM, premisFile.getPronomId());
      }
      // TODO remove file extension
      // if (format.getExtension() != null) {
      // doc.addField(RodaConstants.FILE_EXTENSION, format.getExtension());
      // }
      // TODO add format registry

      if (premisFile.getCreatingApplicationName() != null) {
        doc.addField(RodaConstants.FILE_CREATING_APPLICATION_NAME, premisFile.getCreatingApplicationName());
      }
      if (premisFile.getCreatingApplicationVersion() != null) {
        doc.addField(RodaConstants.FILE_CREATING_APPLICATION_VERSION, premisFile.getCreatingApplicationVersion());
      }
      if (premisFile.getDateCreatedByApplication() != null) {
        doc.addField(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, premisFile.getDateCreatedByApplication());
      }
    }

    if (fulltext != null) {
      doc.addField(RodaConstants.FILE_FULLTEXT, fulltext);

    }

    return doc;
  }

  public static IndexedFile solrDocumentToSimpleFile(SolrDocument doc) {
    IndexedFile file = null;
    String aipId = objectToString(doc.get(RodaConstants.FILE_AIPID));
    String representationId = objectToString(doc.get(RodaConstants.FILE_REPRESENTATIONID));
    String fileId = objectToString(doc.get(RodaConstants.FILE_FILEID));
    boolean entryPoint = objectToBoolean(doc.get(RodaConstants.FILE_ISENTRYPOINT));

    String originalName = objectToString(doc.get(RodaConstants.FILE_ORIGINALNAME));
    List<String> hash = objectToListString(doc.get(RodaConstants.FILE_HASH));
    long size = objectToLong(doc.get(RodaConstants.FILE_SIZE));
    boolean isDirectory = objectToBoolean(doc.get(RodaConstants.FILE_ISDIRECTORY));
    String path = objectToString(doc.get(RodaConstants.FILE_STORAGEPATH));

    // format
    String formatDesignationName = objectToString(doc.get(RodaConstants.FILE_FILEFORMAT));
    String formatDesignationVersion = objectToString(doc.get(RodaConstants.FILE_FORMAT_VERSION));
    String mimetype = objectToString(doc.get(RodaConstants.FILE_FORMAT_MIMETYPE));
    String pronom = objectToString(doc.get(RodaConstants.FILE_PRONOM));
    String extension = objectToString(doc.get(RodaConstants.FILE_EXTENSION));
    // FIXME how to restore format registries
    Map<String, String> formatRegistries = new HashMap<>();

    // technical features
    String creatingApplicationName = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_NAME));
    String creatingApplicationVersion = objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION));
    String dateCreatedByApplication = objectToString(doc.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION));
    String fullText = objectToString(doc.get(RodaConstants.FILE_FULLTEXT));

    FileFormat fileFormat = new FileFormat(formatDesignationName, formatDesignationVersion, mimetype, pronom, extension,
      formatRegistries);
    file = new IndexedFile(fileId, aipId, representationId, path, entryPoint, fileFormat, originalName, size,
      isDirectory, creatingApplicationName, creatingApplicationVersion, dateCreatedByApplication, hash, fullText);
    return file;
  }

  public static SolrInputDocument jobReportToSolrDocument(JobReport jobReport) {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(RodaConstants.JOB_REPORT_ID, jobReport.getId());
    doc.addField(RodaConstants.JOB_REPORT_DATE_CREATED, jobReport.getDateCreated());
    doc.addField(RodaConstants.JOB_REPORT_DATE_UPDATE, jobReport.getDateUpdated());
    doc.addField(RodaConstants.JOB_REPORT_JOB_ID, jobReport.getJobId());
    doc.addField(RodaConstants.JOB_REPORT_AIP_ID, jobReport.getAipId());
    doc.addField(RodaConstants.JOB_REPORT_OBJECT_ID, jobReport.getObjectId());
    doc.addField(RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN, jobReport.getLastPluginRan());
    doc.addField(RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN_STATE, jobReport.getLastPluginRanState());

    doc.addField(RodaConstants.JOB_REPORT_REPORT, ModelUtils.getJsonFromObject(jobReport.getReport()));

    return doc;
  }

  private static JobReport solrDocumentToJobReport(SolrDocument doc) {
    JobReport jobReport = new JobReport();

    jobReport.setId(objectToString(doc.get(RodaConstants.JOB_REPORT_ID)));
    jobReport.setDateCreated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_CREATED)));
    jobReport.setDateUpdated(objectToDate(doc.get(RodaConstants.JOB_REPORT_DATE_UPDATE)));
    jobReport.setJobId(objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_ID)));
    jobReport.setAipId(objectToString(doc.get(RodaConstants.JOB_REPORT_AIP_ID)));
    jobReport.setObjectId(objectToString(doc.get(RodaConstants.JOB_REPORT_OBJECT_ID)));
    jobReport.setLastPluginRan(objectToString(doc.get(RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN)));
    jobReport.setLastPluginRanState(
      PluginState.valueOf(objectToString(doc.get(RodaConstants.JOB_REPORT_LAST_PLUGIN_RAN_STATE))));
    jobReport.setReport(ModelUtils.getJobReportFromJson(objectToString(doc.get(RodaConstants.JOB_REPORT_REPORT))));

    return jobReport;
  }
}
